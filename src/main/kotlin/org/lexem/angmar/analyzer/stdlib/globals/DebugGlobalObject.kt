package org.lexem.angmar.analyzer.stdlib.globals

import es.jtp.kterm.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in Debug global object.
 */
internal object DebugGlobalObject {
    const val ObjectName = "Debug"

    // Methods
    const val Pause = "pause"
    const val Log = "log"
    const val Throw = "throw"

    // Method arguments
    private val LogArgs = listOf("message", "tag")
    private val ThrowArgs = LogArgs

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the global object.
     */
    fun initObject(memory: LexemMemory) {
        val objectValue = LxmObject(memory)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true)
                .setProperty(ObjectName, objectValue, isConstant = true)

        // Methods
        objectValue.setProperty(Pause, LxmFunction(memory, ::pauseFunction), isConstant = true)
        objectValue.setProperty(Log, LxmFunction(memory, ::logFunction), isConstant = true)
        objectValue.setProperty(Throw, LxmFunction(memory, ::throwFunction), isConstant = true)
    }

    /**
     * Pauses the analyzer.
     */
    private fun pauseFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                analyzer.nextNode(InternalFunctionCallCompiled, signal + 1)
                analyzer.status = LexemAnalyzer.Status.Paused

                return false
            }
            else -> {
                return true
            }
        }
    }

    /**
     * Logs a custom message to the console.
     */
    private fun logFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(LogArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val message = parsedArguments[LogArgs[0]]!!
                val tag = parsedArguments[LogArgs[1]]!!

                if (tag != LxmNil && tag !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Log' method requires the parameter called '${LogArgs[1]}' be a ${StringType.TypeName}") {}
                }

                // Calls toString.
                StdlibCommons.callToString(analyzer, message, InternalFunctionCallCompiled,
                        AnalyzerNodesCommons.signalStart + 1, Log)

                return false
            }
            else -> {
                val tag = parsedArguments[LogArgs[1]] as? LxmString
                val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a ${StringType.TypeName}") {}

                Logger("$result") {
                    if (tag != null) {
                        addNote(Consts.Logger.tagTitle, tag.primitive)
                    }
                }.logAsInfo()

                // Always return a value.
                analyzer.memory.replaceLastStackCell(LxmNil)

                return true
            }
        }
    }

    /**
     * Throws a custom error.
     */
    private fun throwFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(ThrowArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val message = parsedArguments[ThrowArgs[0]]!!
                val tag = parsedArguments[ThrowArgs[1]]!!

                if (tag != LxmNil && tag !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Throw' method requires the parameter called '${ThrowArgs[1]}' be a ${StringType.TypeName}") {}
                }

                // Calls toString.
                StdlibCommons.callToString(analyzer, message, InternalFunctionCallCompiled,
                        AnalyzerNodesCommons.signalStart + 1, Log)

                return false
            }
            else -> {
                val tag = parsedArguments[ThrowArgs[1]] as? LxmString
                val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a ${StringType.TypeName}") {}

                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CustomError, "$result") {
                    if (tag != null) {
                        addNote(Consts.Logger.tagTitle, tag.primitive)
                    }
                }
            }
        }
    }
}
