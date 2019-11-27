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
import java.time.*


/**
 * Analyzer for the Lexem language.
 */
class LexemAnalyzer internal constructor(internal val grammarRootNode: ParserNode) {
    var text: IReader = IOStringReader.from("")
        internal set
    internal var rootFilePath = ""
    internal val memory = LexemMemory()
    internal var processStatus = ProcessStatus.Forward
    internal var nextNode: ParserNode? = null
    internal var signal = 0
    internal var backtrackingData: LxmBacktrackingData? = null
    internal var initialCursor: IReaderCursor = text.saveCursor()

    var status = Status.Ended
        internal set
    var entryPoint: String = Consts.defaultEntryPoint
        private set

    init {
        val stdLibContext = LxmContext()
        val stdLibContextReference = memory.add(stdLibContext)
        stdLibContextReference.increaseReferences(memory)

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
     * Starts a new analysis.
     */
    fun start(text: IReader, timeoutInMilliseconds: Long = Consts.Analyzer.defaultTimeoutInMilliseconds): Boolean {
        // Init state
        this.text = text
        memory.clear()

        // Set the rollback code point.
        val stdlibContext = AnalyzerCommons.getStdLibContext(memory)
        stdlibContext.setProperty(memory, AnalyzerCommons.Identifiers.HiddenRollbackCodePoint,
                LxmRollbackCodePoint(grammarRootNode, signal, text.saveCursor()))

        memory.freezeCopy()

        // Init the result node.
        initRootNode()

        processStatus = ProcessStatus.Forward

        nextNode = grammarRootNode
        signal = 0
        backtrackingData = null
        initialCursor = text.saveCursor()

        // Set the main file path.
        rootFilePath = grammarRootNode.parser.reader.getSource()

        // Set the entry point.
        val context = AnalyzerCommons.getCurrentContext(memory)
        context.setProperty(memory, AnalyzerCommons.Identifiers.EntryPoint, LxmString.from(entryPoint))

        // Set the return code point.
        memory.addToStack(AnalyzerCommons.Identifiers.ReturnCodePoint, LxmNil)

        // Execute
        return resume(timeoutInMilliseconds)
    }

    /**
     * Resumes the analysis.
     */
    fun resume(timeoutInMilliseconds: Long = Consts.Analyzer.defaultTimeoutInMilliseconds): Boolean {
        val timeout = OffsetDateTime.now().plusNanos(timeoutInMilliseconds * 1000000)

        try {
            loop@ while (true) {
                // Check timeout.
                val time = OffsetDateTime.now()
                if (time >= timeout || status == Status.Paused) {
                    status = Status.Paused
                    return false
                }

                when (processStatus) {
                    ProcessStatus.Forward -> {
                        // No more nodes
                        if (nextNode == null) {
                            break@loop
                        }

                        nextNode!!.analyze(this, signal)
                    }
                    ProcessStatus.Backward -> {
                        memory.rollbackCopy()

                        val lastCodePoint = getLastRollbackCodePoint()
                        lastCodePoint.restore(this)

                        // Handle the possibility of matching nothing.
                        if (memory.lastNode.previousNode == null) {
                            signal = 0
                            nextNode = null

                            status = Status.Ended
                            return true
                        }

                        processStatus = ProcessStatus.Forward
                    }
                }
            }
        } catch (e: AngmarAnalyzerException) {
            // TODO print the stack traces
            // TODO clear the state.
            throw e
        }

        status = Status.Ended
        return true
    }

    /**
     * Sets a different entry point.
     */
    fun setEntryPoint(entryPoint: String): LexemAnalyzer {
        if (status != Status.Ended) {
            throw AngmarException("Cannot modify a running Analyzer.")
        }

        val context = AnalyzerCommons.getCurrentContext(memory)
        context.setProperty(memory, AnalyzerCommons.Identifiers.EntryPoint, LxmString.from(entryPoint))

        return this
    }

    /**
     * Gets the results of the analyzer.
     */
    fun getResult(): LexemMatch {
        if (status != Status.Ended) {
            throw AngmarException("Cannot get the results of a running Analyzer.")
        }

        // Return the correct result.
        val rootNode = getRootNode()

        return LexemMatch(this, rootNode)
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
     * Inits the backtracking.
     */
    internal fun initBacktracking() {
        this.processStatus = ProcessStatus.Backward
    }

    /**
     * Gets the last rollback code point kept in the memory.
     */
    internal fun getLastRollbackCodePoint() =
            AnalyzerCommons.getCurrentContext(memory).getDereferencedProperty<LxmRollbackCodePoint>(memory,
                    AnalyzerCommons.Identifiers.HiddenRollbackCodePoint)!!

    /**
     * Sets the last rollback code point kept in the memory.
     */
    private fun setLastRollbackCodePoint(codePoint: LxmRollbackCodePoint) =
            AnalyzerCommons.getCurrentContext(memory).setPropertyAsContext(memory,
                    AnalyzerCommons.Identifiers.HiddenRollbackCodePoint, codePoint)

    /**
     * Freezes a new copy of the memory.
     */
    internal fun freezeMemoryCopy(node: ParserNode, signal: Int) {
        setLastRollbackCodePoint(LxmRollbackCodePoint(node, signal, text.saveCursor()))
        memory.freezeCopy()
    }

    /**
     * Restores a frozen memory copy.
     */
    internal fun restoreMemoryCopy(bigNode: BigNode) {
        memory.restoreCopy(bigNode)

        val rollbackCodePoint = getLastRollbackCodePoint()
        rollbackCodePoint.restore(this)
    }

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
        val nodeReference = createNewNode(AnalyzerCommons.Identifiers.Root)
        val analyzerObject = AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, AnalyzerGlobalObject.ObjectName)
        analyzerObject.setProperty(memory, AnalyzerGlobalObject.RootNode, nodeReference)
    }

    /**
     * Creates a new node.
     */
    internal fun createNewNode(name: String): LxmReference {
        val stdlibContext = AnalyzerCommons.getStdLibContext(memory)
        val parentNode = stdlibContext.getPropertyValue(memory,
                AnalyzerCommons.Identifiers.HiddenLastResultNode) as? LxmReference

        val node = LxmNode(name, text.saveCursor(), parentNode, memory)
        val nodeReference = memory.add(node)

        if (parentNode != null) {
            val parent = parentNode.dereferenceAs<LxmNode>(memory)!!
            val parentChildren = parent.getChildren(memory)
            parentChildren.addCell(memory, nodeReference, ignoreConstant = true)
        }

        stdlibContext.setProperty(memory, AnalyzerCommons.Identifiers.HiddenLastResultNode, nodeReference)

        return nodeReference
    }

    /**
     * Sets the upper node as current one.
     */
    internal fun setUpperNode(): LxmReference {
        val stdlibContext = AnalyzerCommons.getStdLibContext(memory)
        val nodeRef =
                stdlibContext.getPropertyValue(memory, AnalyzerCommons.Identifiers.HiddenLastResultNode) as LxmReference
        val node = nodeRef.dereferenceAs<LxmNode>(memory)!!
        val parent = node.getParentReference(memory)!!

        stdlibContext.setProperty(memory, AnalyzerCommons.Identifiers.HiddenLastResultNode, parent)

        return parent
    }

    /**
     * Gets whether the analyzer is running forward.
     */
    internal fun isForward(): Boolean {
        val props = AnalyzerCommons.getCurrentNodeProps(memory)
        val reverse = props.getPropertyValue(memory, AnalyzerCommons.Properties.Reverse) ?: LxmNil
        val reverseLogic = RelationalFunctions.isTruthy(reverse)

        return !reverseLogic
    }

    // STATIC -----------------------------------------------------------------

    /**
     * Possible status of a [LexemAnalyzer].
     */
    enum class Status {
        Paused,
        Ended
    }

    internal enum class ProcessStatus {
        Forward,
        Backward
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
