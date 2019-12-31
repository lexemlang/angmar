package org.lexem.angmar.analyzer.stdlib.globals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*

/**
 * Built-in import global function.
 */
internal object ImportGlobalFunction {
    const val FunctionName = "import"

    // Method arguments
    private val ImportArgs = listOf("path")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the global function.
     */
    fun initFunction(memory: LexemMemory) {
        val function = LxmFunction(memory, ::importFile)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true).setProperty(FunctionName, function, isConstant = true)
    }

    /**
     * Imports another file.
     */
    fun importFile(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int): Boolean {
        val signalEndOfImport = AnalyzerNodesCommons.signalStart + 1

        when (signal) {
            AnalyzerNodesCommons.signalStart, AnalyzerNodesCommons.signalCallFunction -> {
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                val hiddenContext = AnalyzerCommons.getHiddenContext(analyzer.memory, toWrite = false)
                val callerContext =
                        context.getDereferencedProperty<LxmContext>(AnalyzerCommons.Identifiers.HiddenCallerContext,
                                toWrite = false)!!
                val currentFilePath =
                        callerContext.getDereferencedProperty<LxmString>(AnalyzerCommons.Identifiers.HiddenFilePath,
                                toWrite = false)!!

                val parserArguments = arguments.mapArguments(ImportArgs)
                val path = parserArguments[ImportArgs[0]] as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.BadArgumentError,
                        "The $FunctionName method requires the parameter called '${ImportArgs[0]}' be a ${StringType.TypeName}") {}


                // Parse and analyze the code.
                val grammarNode = if (analyzer.importMode == LexemAnalyzer.ImportMode.AllIn) {
                    val parserMap = hiddenContext.getDereferencedProperty<LxmObject>(
                            AnalyzerCommons.Identifiers.HiddenParserMap, toWrite = false)!!

                    val grammarRootNode =
                            parserMap.getDereferencedProperty<LxmGrammarRootNode>(path.primitive, toWrite = false)
                                    ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist,
                                            "The file '${path.primitive}' does not exist") {}

                    grammarRootNode.grammarRootNode
                } else {
                    val reader = AnalyzerCommons.resolveRelativeUriToReader(analyzer, currentFilePath.primitive,
                            path.primitive)

                    // Process only if it has not been previously processed.
                    let {
                        val exports = findPathInMap(analyzer.memory, hiddenContext, reader.getSource())
                        if (exports != null) {
                            analyzer.memory.addToStackAsLast(exports)
                            return true
                        }
                    }

                    try {
                        val parser = LexemParser(reader)
                        val parserNode = LexemFileNode.parse(parser) ?: throw AngmarAnalyzerException(
                                AngmarAnalyzerExceptionType.FileIsNotLexem,
                                "The file '${reader.getSource()}' is not a valid Lexem file") {}
                        val grammarRootNode = LexemFileCompiled.compile(parserNode)

                        grammarRootNode
                    } catch (e: AngmarParserException) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileIsNotLexem,
                                "The file '${reader.getSource()}' is not a valid Lexem file") {
                            this.cause = e.logger
                        }
                    }
                }

                // Analyze the grammar.
                analyzer.nextNode(grammarNode)

                // Add the elements to recover this call.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.ReturnCodePoint,
                        LxmCodePoint(InternalFunctionCallCompiled, signalEndOfImport, callerNode = function.node,
                                callerContextName = "<Native function '$FunctionName'>"))

                return false
            }
        }

        return true
    }

    /**
     * Finds the path in the processed files to avoid repetitions.
     */
    private fun findPathInMap(memory: LexemMemory, context: LxmContext, path: String): LxmReference? {
        val fileMap =
                context.getDereferencedProperty<LxmObject>(AnalyzerCommons.Identifiers.HiddenFileMap, toWrite = false)!!
        return fileMap.getPropertyValue(path) as? LxmReference
    }
}
