package org.lexem.angmar.analyzer.nodes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*

/**
 * Generic commons for the analyzers.
 */
internal object AnalyzerNodesCommons {
    const val signalStart = 0
    const val signalEndFirstCall = signalStart + 1
    const val signalCallFunction = signalStart - 1
    const val signalExitControl = signalCallFunction - 1
    const val signalNextControl = signalExitControl - 1
    const val signalRedoControl = signalNextControl - 1
    const val signalRestartControl = signalRedoControl - 1
    const val signalReturnControl = signalRestartControl - 1

    // METHODS ----------------------------------------------------------------

    /**
     * Calls a function value.
     * Requires the arguments to have at least one reference count.
     */
    fun callFunction(analyzer: LexemAnalyzer, function: ExecutableValue, arguments: LxmReference, node: ParserNode,
            returnSignal: Int) {
        // Save the return position.
        analyzer.memory.pushStack(LxmCodePoint(node, returnSignal))

        // Push the arguments.
        analyzer.memory.pushStackIgnoringReferenceCount(arguments)

        // If it is internal, also push the function to the stack.
        if (function is LxmInternalFunction) {
            analyzer.memory.pushStack(function)
        }

        // Generate an intermediate context that will be removed at the end.
        AnalyzerCommons.createAndAssignNewFunctionContext(analyzer.memory,
                function.parentContext ?: LxmReference.StdLibContext)

        // Call the function
        analyzer.nextNode(function.parserNode, signalCallFunction)
    }

    /**
     * Gets the primitive value resolving the setter.
     */
    fun resolveSetter(memory: LexemMemory, value: LexemPrimitive) = if (value is LexemSetter) {
        value.resolve(memory)
    } else {
        value
    }

    /**
     * Handles the function execution.
     */
    fun functionExecutionController(analyzer: LexemAnalyzer, signal: Int, parameterList: FunctionParameterListNode?,
            block: ParserNode, node: ParserNode, signalEndParameterList: Int, signalEndBlock: Int) {
        when (signal) {
            // Call the function.
            signalCallFunction -> {
                val arguments = analyzer.memory.popStack()
                val lastCodePoint = analyzer.memory.popStack() as LxmCodePoint

                // Save the returned point.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLastCodePoint, lastCodePoint)

                if (parameterList != null) {
                    analyzer.memory.pushStackIgnoringReferenceCount(arguments)

                    return analyzer.nextNode(parameterList)
                }

                // Set the arguments.
                val argumentsDeref = arguments.dereference(analyzer.memory) as LxmArguments
                argumentsDeref.mapArgumentsToContext(analyzer.memory, LxmParameters(), context)

                // Decrease the reference count of the arguments.
                (arguments as LxmReference).decreaseReferenceCount(analyzer.memory)

                return analyzer.nextNode(block)
            }
            // Parse the arguments.
            signalEndParameterList -> {
                return analyzer.nextNode(block)
            }
            // Finalize the calling.
            signalEndBlock -> {
                // Get the last point to return to it.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val lastCodePoint = context.getPropertyValue(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenLastCodePoint) as LxmCodePoint

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

                // Set the returned value.
                analyzer.memory.pushStack(LxmNil)

                return analyzer.nextNode(lastCodePoint)
            }
            // Throw an error.
            signalExitControl, signalNextControl, signalRedoControl, signalRestartControl -> {
                // Get the control
                val control = analyzer.memory.popStack() as LxmControl

                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal,
                        "The ${control.type} control signal has not reached any valid statement.") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.hintTitle
                        highlightSection(control.node.from.position(), control.node.to.position() - 1)
                        message = "Review that this control statement has a matching statement."
                    }

                    if (control.tag != null) {
                        addNote(Consts.Logger.hintTitle, "Check that any statement has the tag: ${control.tag}")

                        val name = AnalyzerCommons.getCurrentContextTag(analyzer.memory)
                        if (name != null && name == control.tag) {
                            addNote(Consts.Logger.hintTitle,
                                    "A tag in the block of a function cannot receive the ${control.type} control signal")
                        }
                    }
                }
            }
            signalReturnControl -> {
                // Get the control
                val control = analyzer.memory.popStack() as LxmControl

                // Get the last point to return to it.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val lastCodePoint = context.getPropertyValue(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenLastCodePoint) as LxmCodePoint

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

                // Set the returned value.
                analyzer.memory.pushStack(control.value!!)

                return analyzer.nextNode(lastCodePoint)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
