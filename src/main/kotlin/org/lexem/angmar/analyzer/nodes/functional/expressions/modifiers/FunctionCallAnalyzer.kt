package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Analyzer for function call i.e. element(expression).
 */
internal object FunctionCallAnalyzer {
    const val signalEndPropertiesExpression = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFirstArgument = signalEndPropertiesExpression + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FunctionCallNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Move Last to Function in the stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Function)

                // Create arguments
                val arguments = LxmArguments(analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

                // Call next element
                if (node.positionalArguments.isNotEmpty()) {
                    return analyzer.nextNode(node.positionalArguments[0])
                }

                if (node.namedArguments.isNotEmpty()) {
                    return analyzer.nextNode(node.namedArguments[0])
                }

                if (node.spreadArguments.isNotEmpty()) {
                    return analyzer.nextNode(node.spreadArguments[0])
                }

                if (node.propertiesExpression != null) {
                    return analyzer.nextNode(node.propertiesExpression)
                }

                return callFunction(analyzer, node)
            }
            // Positional arguments.
            in signalEndFirstArgument until signalEndFirstArgument + node.positionalArguments.size -> {
                val position = (signal - signalEndFirstArgument) + 1

                val value = analyzer.memory.getLastFromStack()
                val arguments =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(analyzer.memory,
                                toWrite = true) as LxmArguments

                arguments.addPositionalArgument(analyzer.memory, value)

                // Remove value from the stack.
                analyzer.memory.removeLastFromStack()

                // Call next element
                if (position < node.positionalArguments.size) {
                    return analyzer.nextNode(node.positionalArguments[position])
                }

                if (node.namedArguments.isNotEmpty()) {
                    return analyzer.nextNode(node.namedArguments[0])
                }

                if (node.spreadArguments.isNotEmpty()) {
                    return analyzer.nextNode(node.spreadArguments[0])
                }

                if (node.propertiesExpression != null) {
                    return analyzer.nextNode(node.propertiesExpression)
                }

                return callFunction(analyzer, node)
            }
            // Named arguments.
            in signalEndFirstArgument + node.positionalArguments.size until signalEndFirstArgument + node.positionalArguments.size + node.namedArguments.size -> {
                val position = (signal - signalEndFirstArgument - node.positionalArguments.size) + 1

                // Call next element
                if (position < node.namedArguments.size) {
                    return analyzer.nextNode(node.namedArguments[position])
                }

                if (node.spreadArguments.isNotEmpty()) {
                    return analyzer.nextNode(node.spreadArguments[0])
                }

                if (node.propertiesExpression != null) {
                    return analyzer.nextNode(node.propertiesExpression)
                }

                return callFunction(analyzer, node)
            }
            // Spread arguments.
            in signalEndFirstArgument + node.positionalArguments.size + node.namedArguments.size until signalEndFirstArgument + node.positionalArguments.size + node.namedArguments.size + node.spreadArguments.size -> {
                val position =
                        (signal - signalEndFirstArgument - node.positionalArguments.size - node.namedArguments.size) + 1

                val value = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false)
                val arguments =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(analyzer.memory,
                                toWrite = true) as LxmArguments

                when (value) {
                    is LxmList -> {
                        val allCells = value.getAllCells()

                        for (i in allCells) {
                            arguments.addPositionalArgument(analyzer.memory, i)
                        }
                    }
                    is LxmObject -> {
                        val allProps = value.getAllIterableProperties()

                        for (i in allProps) {
                            arguments.addNamedArgument(analyzer.memory, i.key, i.value.value)
                        }
                    }
                    else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The value is incorrect for a spread operator.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.spreadArguments[position - 1].from.position(),
                                    node.spreadArguments[position - 1].to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Call next element
                if (position < node.spreadArguments.size) {
                    return analyzer.nextNode(node.spreadArguments[position])
                }

                if (node.propertiesExpression != null) {
                    return analyzer.nextNode(node.propertiesExpression)
                }

                return callFunction(analyzer, node)
            }
            // Properties
            signalEndPropertiesExpression -> {
                return callFunction(analyzer, node)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    private fun callFunction(analyzer: LexemAnalyzer, node: FunctionCallNode) {
        val arguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(analyzer.memory,
                toWrite = true) as LxmArguments
        val functionRef = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Function)

        when (functionRef) {
            is LxmPropertySetter -> {
                arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, functionRef.value)
            }
            is LxmIndexerSetter -> {
                arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, functionRef.element)
            }
            else -> {
                arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, LxmNil)
            }
        }

        val function = AnalyzerNodesCommons.resolveSetter(analyzer.memory, functionRef).dereference(analyzer.memory,
                toWrite = false) as LxmFunction

        // Move Arguments and Function to Auxiliary from the stack.
        analyzer.memory.renameStackCell(AnalyzerCommons.Identifiers.Arguments, AnalyzerCommons.Identifiers.Auxiliary)
        analyzer.memory.renameStackCell(AnalyzerCommons.Identifiers.Function, AnalyzerCommons.Identifiers.Auxiliary)

        // Call the function.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val contextName = AnalyzerCommons.getContextName(analyzer.memory, context)
        AnalyzerNodesCommons.callFunction(analyzer, function, arguments, node,
                LxmCodePoint(node.parent!!, node.parentSignal, callerNode = node,
                        callerContextName = contextName.primitive))

        // Remove Auxiliary two times from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Auxiliary)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Auxiliary)
    }
}
