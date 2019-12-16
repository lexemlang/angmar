package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.setters.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.lexemes.*


/**
 * Analyzer for accesses expression for access lexemes.
 */
internal object AccessExpressionLexemeAnalyzer {
    const val signalEndElement = AnalyzerNodesCommons.signalStart + 1
    const val signalEndFunction = signalEndElement + 1
    const val signalEndFirstModifier = signalEndFunction + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AccessExpressionLexemeNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.element)
            }
            signalEndElement -> {
                // Performs the access if the element is an identifier.
                val id = analyzer.memory.getLastFromStack() as LxmString
                val context = AnalyzerCommons.getCurrentContextReference(analyzer.memory)

                val setter = LxmAccessSetter(analyzer.memory, context, id.primitive, node, node.element)
                analyzer.memory.replaceLastStackCell(setter)

                val finalValueRef = setter.getPrimitive(analyzer.memory)

                // Process the next operand.
                if (node.modifiers.isNotEmpty()) {
                    return analyzer.nextNode(node.modifiers[0])
                } else {
                    // If the setter has a function execute it.
                    val finalValue = finalValueRef.dereference(analyzer.memory, toWrite = false)
                    if (finalValue is ExecutableValue) {
                        val contextName = AnalyzerCommons.getCurrentContextName(analyzer.memory)
                        val arguments = LxmArguments(analyzer.memory)
                        val argumentsRef = analyzer.memory.add(arguments)
                        arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, LxmNil)

                        // Remove Last from the stack.
                        analyzer.memory.removeLastFromStack()

                        return AnalyzerNodesCommons.callFunction(analyzer, finalValueRef, argumentsRef, node,
                                LxmCodePoint(node, signalEndFunction, callerNode = node,
                                        callerContextName = contextName.primitive))
                    }
                }

                analyzer.memory.replaceLastStackCell(finalValueRef)
            }
            signalEndFunction -> {
                // Skip
            }
            in signalEndFirstModifier until signalEndFirstModifier + node.modifiers.size -> {
                val position = (signal - signalEndFirstModifier) + 1

                // Process the next operand
                if (position < node.modifiers.size) {
                    return analyzer.nextNode(node.modifiers[position])
                }

                val result = analyzer.memory.getLastFromStack()
                if (result is LexemSetter) {
                    analyzer.memory.replaceLastStackCell(result.getPrimitive(analyzer.memory))
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
