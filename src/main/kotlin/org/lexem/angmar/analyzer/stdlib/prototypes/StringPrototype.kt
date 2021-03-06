package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * Built-in prototype of the String object.
 */
internal object StringPrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, LxmInternalFunction(::add), isConstant = true)

        prototype.setProperty(memory, AnalyzerCommons.Identifiers.ToString, LxmInternalFunction(::toString),
                isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int): Boolean {
        when (signal) {
            AnalyzerNodesCommons.signalStart, AnalyzerNodesCommons.signalCallFunction -> {
                val parserArguments = arguments.mapArguments(analyzer.memory, AnalyzerCommons.Operators.ParameterList)

                val left = parserArguments[AnalyzerCommons.Identifiers.This] ?: LxmNil
                val right = parserArguments[AnalyzerCommons.Operators.RightParameterName]

                if (left !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                            "The add method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a ${StringType.TypeName}") {}
                }

                if (right == null) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The add method requires the parameter called '${AnalyzerCommons.Operators.RightParameterName}' not to be undefined") {}
                }

                // Push the left operator again to call the toString over the right one.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, left)

                val rightPrototype = right.getObjectOrPrototype(analyzer.memory)
                val functionRef = rightPrototype.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.ToString)
                val function = functionRef?.dereference(analyzer.memory) ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.UndefinedObjectProperty,
                        "The add method requires to call the ${AnalyzerCommons.Identifiers.ToString} method over the '${AnalyzerCommons.Operators.RightParameterName}' operand but it is undefined. Current: $rightPrototype") {}

                function as? ExecutableValue ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.IncompatibleType,
                        "The add method requires to call the ${AnalyzerCommons.Identifiers.ToString} method over the '${AnalyzerCommons.Operators.RightParameterName}' operand but it is not callable i.e. a function or expression. Current: $rightPrototype") {}

                val callArgs = LxmArguments(analyzer.memory)
                val rightRef = analyzer.memory.valueToPrimitive(right)
                callArgs.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, rightRef)
                val callArgsRef = analyzer.memory.add(callArgs)
                callArgsRef.increaseReferences(analyzer.memory)
                AnalyzerNodesCommons.callFunction(analyzer, functionRef, callArgsRef, InternalFunctionCallNode,
                        LxmCodePoint(InternalFunctionCallNode, AnalyzerNodesCommons.signalEndFirstCall))

                return false
            }
            AnalyzerNodesCommons.signalEndFirstCall -> {
                val right = analyzer.memory.getLastFromStack().dereference(analyzer.memory)
                val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left) as LxmString

                if (right !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The ${AnalyzerCommons.Identifiers.ToString} method must return a ${StringType.TypeName}. Current: $right") {}
                }

                val result = left.primitive + right.primitive
                analyzer.memory.replaceLastStackCell(LxmString.from(result))

                // Remove Left from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Left)
            }
        }

        return true
    }

    /**
     * Performs a string concatenation.
     */
    private fun toString(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, AnalyzerCommons.Identifiers.ToString,
                    StringType.TypeName) { _: LexemAnalyzer, thisValue: LxmString ->
                thisValue
            }
}
