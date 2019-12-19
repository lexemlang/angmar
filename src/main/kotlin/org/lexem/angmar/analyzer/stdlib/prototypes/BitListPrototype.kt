package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*

/**
 * Built-in prototype of the BitList object.
 */
internal object BitListPrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.BitwiseNegation, LxmFunction(memory, ::bitwiseNegation),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalAnd, LxmFunction(memory, ::logicalAnd),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalOr, LxmFunction(memory, ::logicalOr),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalXor, LxmFunction(memory, ::logicalXor),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LeftShift, LxmFunction(memory, ::leftShift),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.RightShift, LxmFunction(memory, ::rightShift),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LeftRotate, LxmFunction(memory, ::leftRotate),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.RightRotate, LxmFunction(memory, ::rightRotate),
                isConstant = true)

        return prototype
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs a logical NOT of the 'this' value.
     */
    private fun bitwiseNegation(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, AnalyzerCommons.Operators.BitwiseNegation,
                    BitListType.TypeName, toWrite = false) { _: LexemAnalyzer, thisValue: LxmBitList ->
                LxmBitList(!thisValue.primitive)
            }

    /**
     * Performs a logical AND.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalAnd,
                    BitListType.TypeName, listOf(BitListType.TypeName, LogicType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> LxmBitList(left.primitive.and(right.primitive))
                    is LxmBitList -> LxmBitList(left.primitive.and(right.primitive))
                    else -> null
                }
            }

    /**
     * Performs a logical OR.
     */
    private fun logicalOr(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalOr,
                    BitListType.TypeName, listOf(BitListType.TypeName, LogicType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> LxmBitList(left.primitive.or(right.primitive))
                    is LxmBitList -> LxmBitList(left.primitive.or(right.primitive))
                    else -> null
                }
            }

    /**
     * Performs a logical XOR.
     */
    private fun logicalXor(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalXor,
                    BitListType.TypeName, listOf(BitListType.TypeName, LogicType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> LxmBitList(left.primitive.xor(right.primitive))
                    is LxmBitList -> LxmBitList(left.primitive.xor(right.primitive))
                    else -> null
                }
            }

    /**
     * Performs a left shift.
     */
    private fun leftShift(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LeftShift,
                    BitListType.TypeName, listOf(IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        if (right.primitive < 0) {
                            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                    "The ${AnalyzerCommons.Operators.LeftShift} method requires the parameter called '${AnalyzerCommons.Operators.RightParameterName}' be a positive ${IntegerType.TypeName}") {}
                        }

                        LxmBitList(left.primitive.leftShift(right.primitive))
                    }
                    else -> null
                }
            }

    /**
     * Performs a right shift.
     */
    private fun rightShift(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.RightShift,
                    BitListType.TypeName, listOf(IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        if (right.primitive < 0) {
                            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                    "The ${AnalyzerCommons.Operators.RightShift} method requires the parameter called '${AnalyzerCommons.Operators.RightParameterName}' be a positive ${IntegerType.TypeName}") {}
                        }

                        LxmBitList(left.primitive.rightShift(right.primitive))
                    }
                    else -> null
                }
            }

    /**
     * Performs a left rotate.
     */
    private fun leftRotate(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LeftRotate,
                    BitListType.TypeName, listOf(IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        if (right.primitive < 0) {
                            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                    "The ${AnalyzerCommons.Operators.LeftRotate} method requires the parameter called '${AnalyzerCommons.Operators.RightParameterName}' be a positive ${IntegerType.TypeName}") {}
                        }

                        LxmBitList(left.primitive.leftRotate(right.primitive))
                    }
                    else -> null
                }
            }

    /**
     * Performs a right rotate.
     */
    private fun rightRotate(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, arguments, AnalyzerCommons.Operators.RightRotate,
                    BitListType.TypeName, listOf(IntegerType.TypeName), toWriteLeft = false,
                    toWriteRight = false) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmInteger -> {
                        if (right.primitive < 0) {
                            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                    "The ${AnalyzerCommons.Operators.RightRotate} method requires the parameter called '${AnalyzerCommons.Operators.RightParameterName}' be a positive ${IntegerType.TypeName}") {}
                        }

                        LxmBitList(left.primitive.rightRotate(right.primitive))
                    }
                    else -> null
                }
            }
}
