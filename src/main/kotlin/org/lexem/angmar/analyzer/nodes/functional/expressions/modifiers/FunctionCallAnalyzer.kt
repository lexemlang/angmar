package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
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
                // Create arguments
                val arguments = LxmArguments(analyzer.memory)
                analyzer.memory.pushStack(analyzer.memory.add(arguments))

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

                val value = analyzer.memory.popStack()
                val arguments = analyzer.memory.popStack()
                val argumentsDeref = arguments.dereference(analyzer.memory) as LxmArguments

                argumentsDeref.addPositionalArgument(analyzer.memory, value)

                analyzer.memory.pushStackIgnoringReferenceCount(arguments)

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

                val value = analyzer.memory.popStack()
                val valueDeref = value.dereference(analyzer.memory)
                val arguments = analyzer.memory.popStack()
                val argumentsDeref = arguments.dereference(analyzer.memory) as LxmArguments

                when (valueDeref) {
                    is LxmList -> {
                        val allCells = valueDeref.getAllCells()

                        for (i in allCells) {
                            argumentsDeref.addPositionalArgument(analyzer.memory, i)
                        }
                    }
                    is LxmObject -> {
                        val allProps = valueDeref.getAllIterableProperties()

                        for (i in allProps) {
                            argumentsDeref.addNamedArgument(analyzer.memory, i.key, i.value.value)
                        }
                    }
                    else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The value is incorrect for a spread operator. Actual value: $value") {
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

                analyzer.memory.pushStackIgnoringReferenceCount(arguments)

                // Decrease the reference count of the value.
                (value as LxmReference).decreaseReferenceCount(analyzer.memory)

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
        val arguments = analyzer.memory.popStack() as LxmReference
        val argumentsDeref = arguments.dereference(analyzer.memory) as LxmArguments
        val function = analyzer.memory.popStack()

        when (function) {
            is LxmPropertySetter -> {
                argumentsDeref.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, function.obj)
            }
            is LxmIndexerSetter -> {
                argumentsDeref.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, function.element)
            }
            else -> {
                argumentsDeref.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, LxmNil)
            }
        }

        val functionDeref = function.dereference(analyzer.memory) as? ExecutableValue ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.IncompatibleType,
                "The value is not callable, i.e. a function or expression. Current: $function") {
            val fullText = node.parser.reader.readAllText()
            addSourceCode(fullText, node.parser.reader.getSource()) {
                title = Consts.Logger.codeTitle
                highlightSection(node.from.position(), node.to.position() - 1)
                message = "Cannot perform the invocation of a non-callable element"
            }
        }

        AnalyzerNodesCommons.callFunction(analyzer, functionDeref, arguments, node.parent!!, node.parentSignal)
    }
}
