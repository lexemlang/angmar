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
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalNot, memory.add(LxmFunction(::logicalNot)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalAnd, memory.add(LxmFunction(::logicalAnd)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalOr, memory.add(LxmFunction(::logicalOr)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalXor, memory.add(LxmFunction(::logicalXor)),
                isConstant = true)

        return memory.add(prototype)
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs a logical NOT of the 'this' value.
     */
    private fun logicalNot(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalNot, LogicType.TypeName) { _: LexemAnalyzer, thisValue: LxmLogic ->
                if (thisValue.primitive) {
                    LxmLogic.False
                } else {
                    LxmLogic.True
                }
            }

    /**
     * Performs a logical AND between two logical values.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalAnd, LogicType.TypeName,
                    listOf(LogicType.TypeName)) { _: LexemAnalyzer, left: LxmLogic, right: LexemMemoryValue ->
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
    private fun logicalOr(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalOr, LogicType.TypeName,
                    listOf(LogicType.TypeName)) { _: LexemAnalyzer, left: LxmLogic, right: LexemMemoryValue ->
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
    private fun logicalXor(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalXor, LogicType.TypeName,
                    listOf(LogicType.TypeName)) { _: LexemAnalyzer, left: LxmLogic, right: LexemMemoryValue ->
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
