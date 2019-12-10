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
    private val ThrowArgs = listOf("message")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the global object.
     */
    fun initObject(memory: LexemMemory) {
        val objectValue = LxmObject()
        val reference = memory.add(objectValue)
        AnalyzerCommons.getCurrentContext(memory).setProperty(memory, ObjectName, reference, isConstant = true)

        // Methods
        objectValue.setProperty(memory, Pause, memory.add(LxmFunction(::pauseFunction)), isConstant = true)
        objectValue.setProperty(memory, Log, memory.add(LxmFunction(::logFunction)), isConstant = true)
        objectValue.setProperty(memory, Throw, memory.add(LxmFunction(::throwFunction)), isConstant = true)
    }

    /**
     * Pauses the analyzer.
     */
    private fun pauseFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                analyzer.nextNode(InternalFunctionCallNode, signal + 1)
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
    private fun logFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory, LogArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val message = parsedArguments[LogArgs[0]]!!
                val tag = parsedArguments[LogArgs[1]]!!

                if (tag != LxmNil && tag !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$ObjectName${AccessExplicitMemberNode.accessToken}$Log' method requires the parameter called '${LogArgs[1]}' be a ${StringType.TypeName}") {}
                }

                // Calls toString.
                StdlibCommons.callToString(analyzer, message, AnalyzerNodesCommons.signalStart + 1, Log)

                return false
            }
            else -> {
                val tag = parsedArguments[LogArgs[1]] as? LxmString
                val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a ${StringType.TypeName}") {}

                Logger("$result") {
                    if (tag != null) {
                        addNote(Consts.Logger.errorIdTitle, tag.primitive)
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
    private fun throwFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val parsedArguments =
                        argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory)!!.mapArguments(analyzer.memory,
                                ThrowArgs)

                // Calls toString.
                val message = parsedArguments[ThrowArgs[0]]!!
                StdlibCommons.callToString(analyzer, message, AnalyzerNodesCommons.signalStart + 1, Throw)

                return false
            }
            else -> {
                val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.ToStringMethodNotReturningString,
                        "The ${AnalyzerCommons.Identifiers.ToString} method must always return a ${StringType.TypeName}") {}

                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CustomError, "$result") {}
            }
        }
    }
}
