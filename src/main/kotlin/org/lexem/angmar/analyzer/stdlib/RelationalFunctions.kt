package org.lexem.angmar.analyzer.stdlib

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * Relational functions for Lexem values.
 */
internal object RelationalFunctions {

    /**
     * Checks whether an object is truthy or not.
     */
    fun isTruthy(value: LexemMemoryValue) = if (value is LexemSetter) {
        throw AngmarUnreachableException()
    } else {
        value != LxmNil && value != LxmLogic.False
    }

    /**
     * Compares the equality of both values.
     */
    fun lxmEquals(memory: LexemMemory, leftValue: LexemMemoryValue, rightValue: LexemMemoryValue): Boolean {
        val left = leftValue.getPrimitive()
        val right = rightValue.getPrimitive()

        if (identityEquals(left, right)) {
            return true
        }

        if (left is LxmReference && right is LxmReference) {
            val leftDeref = leftValue.dereference(memory, toWrite = false)
            val rightDeref = rightValue.dereference(memory, toWrite = false)

            when {
                leftDeref is LxmList && rightDeref is LxmList -> {
                    if (leftDeref.isConstant != rightDeref.isConstant) {
                        return false
                    }

                    if (leftDeref.size != rightDeref.size) {
                        return false
                    }

                    for (i in leftDeref.getAllCells().zip(rightDeref.getAllCells())) {
                        if (identityNotEquals(i.first, i.second)) {
                            return false
                        }
                    }

                    return true
                }
                leftDeref is LxmSet && rightDeref is LxmSet -> {
                    if (leftDeref.isConstant != rightDeref.isConstant) {
                        return false
                    }

                    if (leftDeref.size != rightDeref.size) {
                        return false
                    }

                    val leftValues = leftDeref.getAllValues()
                    val rightValues = rightDeref.getAllValues()

                    for (i in leftValues) {
                        if (i !in rightValues) {
                            return false
                        }
                    }

                    return true
                }
                leftDeref is LxmObject && rightDeref is LxmObject -> {
                    if (leftDeref.isConstant != rightDeref.isConstant) {
                        return false
                    }

                    if (leftDeref.size != rightDeref.size) {
                        return false
                    }

                    val leftValues = leftDeref.getAllIterableProperties()
                    val rightValues = rightDeref.getAllIterableProperties()

                    for ((key, property) in leftValues) {
                        val rightProperty = rightValues[key] ?: return false

                        if (property.isConstant != rightProperty.isConstant) {
                            return false
                        }

                        if (identityNotEquals(property.value, rightProperty.value)) {
                            return false
                        }
                    }

                    return true
                }
                leftDeref is LxmMap && rightDeref is LxmMap -> {
                    if (leftDeref.isConstant != rightDeref.isConstant) {
                        return false
                    }

                    if (leftDeref.size != rightDeref.size) {
                        return false
                    }

                    val leftValues = leftDeref.getAllProperties()
                    val rightValues = rightDeref.getAllProperties()

                    for ((key, lValue) in leftValues) {
                        val (_, rValue) = rightValues.first { identityEquals(it.first, key) } ?: return false

                        if (!identityEquals(lValue, rValue)) {
                            return false
                        }
                    }

                    return true
                }
            }
        }

        return false
    }

    /**
     * Compares the inequality of both values.
     */
    fun lxmNotEquals(memory: LexemMemory, left: LexemMemoryValue, right: LexemMemoryValue) =
            !lxmEquals(memory, left, right)

    /**
     * Compares the equality of the identity of both values.
     */
    fun identityEquals(leftValue: LexemMemoryValue, rightValue: LexemMemoryValue): Boolean {
        if (leftValue is LexemSetter || rightValue is LexemSetter) {
            throw AngmarUnreachableException()
        }

        val left = leftValue.getPrimitive()
        val right = rightValue.getPrimitive()

        return when (left) {
            is LxmNil -> right is LxmNil
            is LxmLogic -> right is LxmLogic && left.primitive == right.primitive
            is LxmInteger -> right is LxmInteger && left.primitive == right.primitive
            is LxmFloat -> right is LxmFloat && left.primitive == right.primitive
            is LxmString -> right is LxmString && left.primitive == right.primitive
            is LxmReference -> right is LxmReference && left.position == right.position
            else -> false
        }
    }

    /**
     * Compares the inequality of the identity of both values.
     */
    fun identityNotEquals(left: LexemMemoryValue, right: LexemMemoryValue) = !identityEquals(left, right)

    /**
     * Compares whether left is lower than right.
     */
    fun lowerThan(left: LexemMemoryValue, right: LexemMemoryValue) = if (left is LexemSetter || right is LexemSetter) {
        throw AngmarUnreachableException()
    } else {
        when (left) {
            is LxmInteger -> when (right) {
                is LxmInteger -> left.primitive < right.primitive
                is LxmFloat -> left.primitive < right.primitive
                else -> false
            }
            is LxmFloat -> when (right) {
                is LxmInteger -> left.primitive < right.primitive
                is LxmFloat -> left.primitive < right.primitive
                else -> false
            }
            else -> false
        }
    }

    /**
     * Compares whether left is greater than right.
     */
    fun greaterThan(left: LexemMemoryValue, right: LexemMemoryValue) =
            if (left is LexemSetter || right is LexemSetter) {
                throw AngmarUnreachableException()
            } else {
                when (left) {
                    is LxmInteger -> when (right) {
                        is LxmInteger -> left.primitive > right.primitive
                        is LxmFloat -> left.primitive > right.primitive
                        else -> false
                    }
                    is LxmFloat -> when (right) {
                        is LxmInteger -> left.primitive > right.primitive
                        is LxmFloat -> left.primitive > right.primitive
                        else -> false
                    }
                    else -> false
                }
            }


    /**
     * Compares whether left is lower or equal than right.
     */
    fun lowerOrEqualThan(left: LexemMemoryValue, right: LexemMemoryValue) = !greaterThan(left, right)

    /**
     * Compares whether left is greater or equal than right.
     */
    fun greaterOrEqualThan(left: LexemMemoryValue, right: LexemMemoryValue) = !lowerThan(left, right)
}
