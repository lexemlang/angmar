package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import java.util.*
import kotlin.math.*

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
                val result = thisValue.primitive.clone() as BitSet
                result.flip(0, thisValue.size)

                LxmBitList(thisValue.size, result)
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
                    is LxmLogic -> {
                        if (right.primitive) {
                            left
                        } else {
                            val result = left.primitive.clone() as BitSet
                            result.clear()
                            LxmBitList(left.size, result)
                        }
                    }
                    is LxmBitList -> {
                        val result = left.primitive.clone() as BitSet
                        result.and(right.primitive)
                        LxmBitList(max(left.size, right.size), result)
                    }
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
                    is LxmLogic -> {
                        if (right.primitive) {
                            val result = left.primitive.clone() as BitSet
                            result.set(0, left.size)
                            LxmBitList(left.size, result)
                        } else {
                            left
                        }
                    }
                    is LxmBitList -> {
                        val result = left.primitive.clone() as BitSet
                        result.or(right.primitive)
                        LxmBitList(max(left.size, right.size), result)
                    }
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
                    is LxmLogic -> {
                        if (right.primitive) {
                            val result = left.primitive.clone() as BitSet
                            result.flip(0, left.size)
                            LxmBitList(left.size, result)
                        } else {
                            left
                        }
                    }
                    is LxmBitList -> {
                        val result = left.primitive.clone() as BitSet
                        result.xor(right.primitive)
                        LxmBitList(max(left.size, right.size), result)
                    }
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

                        if (left.size <= right.primitive) {
                            return@executeBinaryOperator LxmBitList.Empty
                        }

                        val result = BitSet(left.size - right.primitive)

                        for (i in right.primitive until left.size) {
                            result[i - right.primitive] = left.primitive[i]
                        }

                        LxmBitList(left.size - right.primitive, result)
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

                        val result = BitSet(left.size + right.primitive)

                        for (i in 0 until left.size) {
                            result[right.primitive + i] = left.primitive[i]
                        }

                        LxmBitList(left.size + right.primitive, result)
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

                        val movement = right.primitive % left.size

                        if (movement == 0) {
                            return@executeBinaryOperator left
                        }

                        val result = BitSet(left.size)

                        val end = left.size - movement
                        for (i in 0 until end) {
                            result[i] = left.primitive[i + movement]
                        }

                        for (i in end until left.size) {
                            result[i] = left.primitive[i - end]
                        }

                        LxmBitList(left.size, result)
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

                        val movement = right.primitive % left.size

                        if (movement == 0) {
                            return@executeBinaryOperator left
                        }

                        val result = BitSet(left.size)

                        val start = left.size - movement
                        for (i in start until left.size) {
                            result[i - start] = left.primitive[i]
                        }

                        for (i in 0 until start) {
                            result[i + movement] = left.primitive[i]
                        }

                        LxmBitList(left.size, result)
                    }
                    else -> null
                }
            }
}
