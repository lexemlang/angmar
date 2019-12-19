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
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)

        // Methods
        prototype.setProperty(memory, IsEmpty, LxmFunction(memory, ::isEmptyFunction), isConstant = true)
        prototype.setProperty(memory, PointCount, LxmFunction(memory, ::pointCountFunction), isConstant = true)
        prototype.setProperty(memory, UnicodeNot, LxmFunction(memory, ::unicodeNotFunction), isConstant = true)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalNot, LxmFunction(memory, ::logicalNot),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalAnd, LxmFunction(memory, ::logicalAnd),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalOr, LxmFunction(memory, ::logicalOr),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalXor, LxmFunction(memory, ::logicalXor),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Add, LxmFunction(memory, ::add), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.Sub, LxmFunction(memory, ::sub), isConstant = true)

        return prototype
    }

    /**
     * Performs a unicode logical NOT of the 'this' value.
     */
    private fun isEmptyFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, IsEmpty, IntervalType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmInterval ->
                LxmLogic.from(thisValue.primitive.isEmpty)
            }

    /**
     * Performs a unicode logical NOT of the 'this' value.
     */
    private fun pointCountFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, PointCount, IntervalType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmInterval ->
                LxmInteger.from(thisValue.primitive.pointCount.toInt()) // Long to int can cause errors
            }

    /**
     * Performs a unicode logical NOT of the 'this' value.
     */
    private fun unicodeNotFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, UnicodeNot, IntervalType.TypeName,
                    toWrite = false) { _: LexemAnalyzer, thisValue: LxmInterval ->
                LxmInterval.from(thisValue.primitive.unicodeNot())
            }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs a logical NOT of the 'this' value.
     */
    private fun logicalNot(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalNot,
                    IntervalType.TypeName, toWrite = false) { _: LexemAnalyzer, thisValue: LxmInterval ->
                LxmInterval.from(thisValue.primitive.not())
            }

    /**
     * Performs a logical AND between two values.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalAnd,
                    IntervalType.TypeName, listOf(IntervalType.TypeName, IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
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
    private fun logicalOr(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalOr,
                    IntervalType.TypeName, listOf(IntervalType.TypeName, IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
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
    private fun logicalXor(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalXor,
                    IntervalType.TypeName, listOf(IntervalType.TypeName, IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
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
    private fun add(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Add,
                    IntervalType.TypeName, listOf(IntervalType.TypeName, IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
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
    private fun sub(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.Sub,
                    IntervalType.TypeName, listOf(IntervalType.TypeName, IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmInterval, right: LexemMemoryValue ->
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
