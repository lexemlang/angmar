package org.lexem.angmar

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.globals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*

/**
 * Analyzer for the Lexem language.
 */
class LexemAnalyzer internal constructor(internal val grammarRootNode: ParserNode) {
    var text: ITextReader = CustomStringReader.from("")
        private set
    internal var rootFilePath = ""
    internal val memory = LexemMemory()
    internal var status = AnalyzerStatus.Forward
    internal var nextNode: ParserNode? = null
    internal var signal = 0

    init {
        val stdLibContext = LxmContext()
        val stdLibContextReference = memory.add(stdLibContext)
        stdLibContextReference.increaseReferenceCount(memory)

        if (stdLibContextReference.position != 0) {
            // This must never happen.
            throw AngmarUnreachableException()
        }

        stdLibContext.setProperty(memory, AnalyzerCommons.Identifiers.HiddenCurrentContext, stdLibContextReference)

        val fileMap = LxmObject()
        val fileMapReference = memory.add(fileMap)
        stdLibContext.setProperty(memory, AnalyzerCommons.Identifiers.HiddenFileMap, fileMapReference)

        StdlibCommons.initTypesAndPrototypes(this.memory)
        StdlibCommons.initGlobals(this.memory)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Executes the analysis.
     */
    fun process(text: ITextReader, entryPoint: String = Consts.defaultEntryPoint): List<LexemMatch> {
        // Init state
        this.text = text
        memory.clear()
        initRootNode()

        status = AnalyzerStatus.Forward

        nextNode = grammarRootNode
        signal = 0

        // Set the main file path.
        rootFilePath = grammarRootNode.parser.reader.getSource()

        // Set the entry point.
        val context = AnalyzerCommons.getCurrentContext(memory)
        context.setProperty(memory, AnalyzerCommons.Identifiers.EntryPoint, LxmString.from(entryPoint))

        // Execute
        try {
            loop@ while (true) {
                when (status) {
                    AnalyzerStatus.Forward -> {
                        // No more nodes
                        if (nextNode == null) {
                            break@loop
                        }

                        nextNode!!.analyze(this, signal)
                    }
                    AnalyzerStatus.Backward -> {
                        try {
                            memory.rollbackCopy()
                        } catch (e: AngmarAnalyzerException) {
                            // Handle the possibility of matching nothing.
                            return emptyList()
                        }

                        val lastCodePoint = getLastRollbackCodePoint()

                        nextNode = lastCodePoint.node
                        signal = lastCodePoint.signal
                        lastCodePoint.readerCursor.restore()

                        status = AnalyzerStatus.Forward
                    }
                    AnalyzerStatus.Exit -> {
                        // Manages the finish signal.
                        return emptyList()
                    }
                }
            }
        } catch (e: AngmarAnalyzerException) {
            // TODO print the stack traces
            throw e
        }

        // Return the correct result.
        val rootNode = getRootNode()
        val children = rootNode.getChildrenAsList(memory)

        if (children.isNotEmpty()) {
            return children.map { position ->
                LexemMatch(this, position.dereference(memory) as LxmNode)
            }
        }

        return emptyList()
    }

    /**
     * Sets the next node to execute.
     */
    internal fun nextNode(nextNode: ParserNode?, signal: Int = AnalyzerNodesCommons.signalStart) {
        this.nextNode = nextNode
        this.signal = signal
    }

    /**
     * Sets the next node to execute.
     */
    internal fun nextNode(codePoint: LxmCodePoint) {
        this.nextNode = codePoint.node
        this.signal = codePoint.signal
    }

    /**
     * Gets the last rollback code point kept in the memory.
     */
    private fun getLastRollbackCodePoint() =
            AnalyzerCommons.getCurrentContext(memory).getDereferencedProperty<LxmRollbackCodePoint>(memory,
                    AnalyzerCommons.Identifiers.HiddenLastCodePoint)!!

    /**
     * Gets the root [LxmNode] object.
     */
    private fun getRootNode(): LxmNode {
        val analyzerObject = AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, AnalyzerGlobalObject.ObjectName)
        return analyzerObject.getDereferencedProperty(memory, AnalyzerGlobalObject.RootNode)!!
    }

    /**
     * Creates the root node.
     */
    private fun initRootNode() {
        val node = LxmNode(AnalyzerCommons.Identifiers.Root, text.saveCursor()).init(memory)
        val nodeReference = memory.add(node)
        val analyzerObject = AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, AnalyzerGlobalObject.ObjectName)
        analyzerObject.setProperty(memory, AnalyzerGlobalObject.RootNode, nodeReference)
    }

    // STATIC -----------------------------------------------------------------

    enum class AnalyzerStatus {
        Forward,
        Backward,
        Exit
    }

    companion object {
        /**
         * Creates a new [LexemAnalyzer] from the specified input.
         * @param forwardBuffer Whether to activate a buffer to accelerate the parsing.
         */
        fun parse(text: ITextReader, forwardBuffer: Boolean = true): LexemAnalyzer? {
            val parser = LexemParser(text, forwardBuffer)
            val parserNode = LexemFileNode.parse(parser) ?: return null

            return LexemAnalyzer(parserNode)
        }
    }
}
