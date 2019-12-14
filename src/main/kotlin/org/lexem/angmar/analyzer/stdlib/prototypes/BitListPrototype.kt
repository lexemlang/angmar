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
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.BitwiseNegation,
                memory.add(LxmFunction(::bitwiseNegation)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalAnd, memory.add(LxmFunction(::logicalAnd)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalOr, memory.add(LxmFunction(::logicalOr)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalXor, memory.add(LxmFunction(::logicalXor)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LeftShift, memory.add(LxmFunction(::leftShift)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.RightShift, memory.add(LxmFunction(::rightShift)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.LeftRotate, memory.add(LxmFunction(::leftRotate)),
                isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.RightRotate, memory.add(LxmFunction(::rightRotate)),
                isConstant = true)

        return memory.add(prototype)
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs a logical NOT of the 'this' value.
     */
    private fun bitwiseNegation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.BitwiseNegation,
                    BitListType.TypeName) { _: LexemAnalyzer, thisValue: LxmBitList ->
                LxmBitList(!thisValue.primitive)
            }

    /**
     * Performs a logical AND.
     */
    private fun logicalAnd(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalAnd, BitListType.TypeName, listOf(BitListType.TypeName,
                    LogicType.TypeName)) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> LxmBitList(left.primitive.and(right.primitive))
                    is LxmBitList -> LxmBitList(left.primitive.and(right.primitive))
                    else -> null
                }
            }

    /**
     * Performs a logical OR.
     */
    private fun logicalOr(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalOr, BitListType.TypeName, listOf(BitListType.TypeName,
                    LogicType.TypeName)) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> LxmBitList(left.primitive.or(right.primitive))
                    is LxmBitList -> LxmBitList(left.primitive.or(right.primitive))
                    else -> null
                }
            }

    /**
     * Performs a logical XOR.
     */
    private fun logicalXor(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LogicalXor, BitListType.TypeName, listOf(BitListType.TypeName,
                    LogicType.TypeName)) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
                when (right) {
                    is LxmLogic -> LxmBitList(left.primitive.xor(right.primitive))
                    is LxmBitList -> LxmBitList(left.primitive.xor(right.primitive))
                    else -> null
                }
            }

    /**
     * Performs a left shift.
     */
    private fun leftShift(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LeftShift, BitListType.TypeName,
                    listOf(IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
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
    private fun rightShift(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.RightShift, BitListType.TypeName,
                    listOf(IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
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
    private fun leftRotate(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.LeftRotate, BitListType.TypeName,
                    listOf(IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
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
    private fun rightRotate(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeBinaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.RightRotate, BitListType.TypeName,
                    listOf(IntegerType.TypeName)) { _: LexemAnalyzer, left: LxmBitList, right: LexemMemoryValue ->
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
