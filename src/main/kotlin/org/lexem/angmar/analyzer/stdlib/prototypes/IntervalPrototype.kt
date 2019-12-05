package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * Built-in prototype of the Interval object.
 */
internal object IntervalPrototype {
    // Methods
    const val IsEmpty = "isEmpty"
    const val PointCount = "pointCount"
    const val UnicodeNot = "unicodeNot"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

        // Methods
        prototype.setProperty(memory, IsEmpty, memory.add(LxmFunction(::isEmptyFunction)), isConstant = true)
        prototype.setProperty(memory, PointCount, memory.add(LxmFunction(::pointCountFunction)), isConstant = true)
        prototype.setProperty(memory, UnicodeNot, memory.add(LxmFunction(::unicodeNotFunction)), isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalNot, memory.add(LxmFunction(::logicalNot)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalAnd, memory.add(LxmFunction(::logicalAnd)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalOr, memory.add(LxmFunction(::logicalOr)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalXor, memory.add(LxmFunction(::logicalXor)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, memory.add(LxmFunction(::add)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Sub, memory.add(LxmFunction(::sub)), isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Performs a unicode logical NOT of the 'this' value.
     */
    private fun isEmptyFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    IsEmpty, IntervalType.TypeName) { _: LexemAnalyzer, thisValue: LxmInterval ->
                LxmLogic.from(thisValue.primitive.isEmpty)
            }

    /**
     * Performs a unicode logical NOT of the 'this' value.
     */
    private fun pointCountFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    PointCount, IntervalType.TypeName) { _: LexemAnalyzer, thisValue: LxmInterval ->
                LxmInteger.from(thisValue.primitive.pointCount.toInt()) // Long to int can cause errors
            }

    /**
     * Performs a unicode logical NOT of the 'this' value.
     */
    private fun unicodeNotFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    UnicodeNot, IntervalType.TypeName) { _: LexemAnalyzer, thisValue: LxmInterval ->
                LxmInterval.from(thisValue.primitive.unicodeNot())
            }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs a logical NOT of the 'this' value.
     */
    private fun logicalNot(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalNot,
                    IntervalType.TypeName) { _: LexemAnalyzer, thisValue: LxmInterval ->
                LxmInterval.from(thisValue.primitive.not())
            }

    /**
     * Performs a logical AND between two values.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalAnd, IntervalType.TypeName, listOf(IntervalType.TypeName,
                    IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        LxmInterval.from(left.primitive.common(right.primitive))
                    }
                    is LxmInterval -> {
                        LxmInterval.from(left.primitive.common(right.primitive))
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical OR between two values.
     */
    private fun logicalOr(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalOr, IntervalType.TypeName, listOf(IntervalType.TypeName,
                    IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        LxmInterval.from(left.primitive.plus(right.primitive))
                    }
                    is LxmInterval -> {
                        LxmInterval.from(left.primitive.plus(right.primitive))
                    }
                    else -> null
                }
            }

    /**
     * Performs a logical XOR between two values.
     */
    private fun logicalXor(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalXor, IntervalType.TypeName, listOf(IntervalType.TypeName,
                    IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        LxmInterval.from(left.primitive.notCommon(right.primitive))
                    }
                    is LxmInterval -> {
                        LxmInterval.from(left.primitive.notCommon(right.primitive))
                    }
                    else -> null
                }
            }

    /**
     * Performs the addition of two values.
     */
    private fun add(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Add, IntervalType.TypeName, listOf(IntervalType.TypeName,
                    IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        LxmInterval.from(left.primitive.plus(right.primitive))
                    }
                    is LxmInterval -> {
                        LxmInterval.from(left.primitive.plus(right.primitive))
                    }
                    else -> null
                }
            }

    /**
     * Performs the subtraction of two values.
     */
    private fun sub(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.Sub, IntervalType.TypeName, listOf(IntervalType.TypeName,
                    IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        LxmInterval.from(left.primitive.minus(right.primitive))
                    }
                    is LxmInterval -> {
                        LxmInterval.from(left.primitive.minus(right.primitive))
                    }
                    else -> null
                }
            }
}
