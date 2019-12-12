package org.lexem.angmar.analyzer.stdlib.globals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in import global function.
 */
internal object ImportGlobalFunction {
    const val FunctionName = "import"
    const val PathParam = "path"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the global function.
     */
    fun initFunction(memory: LexemMemory) {
        val function = LxmFunction(::importFile)
        val functionRef = memory.add(function)
        AnalyzerCommons.getCurrentContext(memory).setProperty(memory, FunctionName, functionRef, isConstant = true)
    }

    /**
     * Imports another file.
     */
    fun importFile(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val signalEndOfImport = AnalyzerNodesCommons.signalStart + 1

        when (signal) {
            AnalyzerNodesCommons.signalStart, AnalyzerNodesCommons.signalCallFunction -> {
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val callerContext = context.getDereferencedProperty<LxmContext>(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenCallerContext)!!
                val currentFilePath = callerContext.getDereferencedProperty<LxmString>(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenFilePath)!!

                val parserArguments =
                        argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                                listOf(PathParam))
                val path = parserArguments[PathParam] as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.BadArgumentError,
                        "The $FunctionName method requires the parameter called '$PathParam' be a ${StringType.TypeName}") {}


                // Parse and analyze the code.
                val finalSource: String
                val parser = if (analyzer.importMode == LexemAnalyzer.ImportMode.AllIn) {
                    val parserMap = AnalyzerCommons.getStdLibContextElement<LxmObject>(analyzer.memory,
                            AnalyzerCommons.Identifiers.HiddenParserMap)

                    val parser = parserMap.getDereferencedProperty<LxmParser>(analyzer.memory, path.primitive)
                            ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist,
                                    "The file '${path.primitive}' does not exist") {}

                    finalSource = path.primitive

                    parser.parser
                } else {
                    val reader = AnalyzerCommons.resolveRelativeUriToReader(analyzer, currentFilePath.primitive,
                            path.primitive)

                    // Process only if it has not been previously processed.
                    let {
                        val exports = findPathInMap(analyzer.memory, context, reader.getSource())
                        if (exports != null) {
                            analyzer.memory.addToStackAsLast(exports)
                            return true
                        }
                    }

                    finalSource = reader.getSource()

                    LexemParser(reader)
                }

                try {
                    val grammarNode = LexemFileNode.parse(parser) ?: throw AngmarAnalyzerException(
                            AngmarAnalyzerExceptionType.FileIsNotLexem,
                            "The file '$finalSource' is not a valid Lexem file") {}

                    analyzer.nextNode(grammarNode)
                } catch (e: AngmarParserException) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FileIsNotLexem,
                            "The file '$finalSource' is not a valid Lexem file") {
                        this.cause = e.logger
                    }
                }

                // Add the elements to recover this call.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.ReturnCodePoint,
                        LxmCodePoint(InternalFunctionCallNode, signalEndOfImport, callerNode = function.node,
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
        val fileMap = context.getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.HiddenFileMap)!!
        return fileMap.getPropertyValue(memory, path) as? LxmReference
    }
}
