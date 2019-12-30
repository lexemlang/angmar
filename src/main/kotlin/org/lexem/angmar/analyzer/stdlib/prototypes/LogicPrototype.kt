package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * Built-in prototype of the Logic object.
 */
internal object LogicPrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)

        // Operators
        prototype.setProperty( AnalyzerCommons.Operators.LogicalNot, LxmFunction(memory, ::logicalNot),
                isConstant = true)
        prototype.setProperty( AnalyzerCommons.Operators.LogicalAnd, LxmFunction(memory, ::logicalAnd),
                isConstant = true)
        prototype.setProperty( AnalyzerCommons.Operators.LogicalOr, LxmFunction(memory, ::logicalOr),
                isConstant = true)
        prototype.setProperty( AnalyzerCommons.Operators.LogicalXor, LxmFunction(memory, ::logicalXor),
                isConstant = true)

        return prototype
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs a logical NOT of the 'this' value.
     */
    private fun logicalNot(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalNot,
                    LogicType.TypeName, toWrite = false) { _: LexemAnalyzer, thisValue: LxmLogic ->
                if (thisValue.primitive) {
                    LxmLogic.False
                } else {
                    LxmLogic.True
                }
            }

    /**
     * Performs a logical AND between two logical values.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalAnd,
                    LogicType.TypeName, listOf(LogicType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmLogic, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmLogic.from(leftValue.and(rightValue))
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical OR between two logical values.
     */
    private fun logicalOr(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalOr,
                    LogicType.TypeName, listOf(LogicType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmLogic, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmLogic.from(leftValue.or(rightValue))
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical XOR between two logical values.
     */
    private fun logicalXor(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalXor,
                    LogicType.TypeName, listOf(LogicType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmLogic, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> {
                        val leftValue = left.primitive
                        val rightValue = right.primitive

                        LxmLogic.from(leftValue.xor(rightValue))
                    }
                    else -> null
                }
            }
}
