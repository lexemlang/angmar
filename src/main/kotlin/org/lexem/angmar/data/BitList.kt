package org.lexem.angmar.data

import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.*

/**
 * An immutable list of bits.
 */
class BitList internal constructor(val size: Int, internal val content: BitSet) : Iterable<Boolean> {

    /**
     * Gets the bit at the specified position.
     */
    operator fun get(index: Int): Boolean {
        if (index < 0 || index >= size) {
            throw Exception("Cannot get a value outside the bounds of the BitList. Index: $index, Size: $size")
        }

        return content[index]
    }

    /**
     * Flips all the bits.
     */
    operator fun not(): BitList {
        val res = clone()
        res.content.flip(0, size)
        return res
    }

    /**
     * Performs the and operator bit a bit.
     */
    fun and(boolean: Boolean) = if (boolean) {
        this
    } else {
        val res = clone()
        res.content.clear()
        res
    }

    /**
     * Performs the and operator bit a bit.
     */
    fun and(other: BitList): BitList {
        val resultSize = maxOf(size, other.size)
        val res = BitList(resultSize, content.clone() as BitSet)
        res.content.and(other.content)
        return res
    }

    /**
     * Performs the and operator bit a bit.
     */
    fun or(boolean: Boolean) = if (!boolean) {
        this
    } else {
        val res = clone()
        res.content.set(0, size)
        res
    }

    /**
     * Performs the and operator bit a bit.
     */
    fun or(other: BitList): BitList {
        val resultSize = maxOf(size, other.size)
        val res = BitList(resultSize, content.clone() as BitSet)
        res.content.or(other.content)
        return res
    }

    /**
     * Performs the and operator bit a bit.
     */
    fun xor(boolean: Boolean) = if (!boolean) {
        this
    } else {
        val res = clone()
        res.content.flip(0, size)
        res
    }

    /**
     * Performs the and operator bit a bit.
     */
    fun xor(other: BitList): BitList {
        val resultSize = maxOf(size, other.size)
        val res = BitList(resultSize, content.clone() as BitSet)
        res.content.xor(other.content)
        return res
    }

    /**
     * Moves the bits to the left removing those bits that leave the list and setting 0s in the new positions.
     */
    fun leftShift(displacement: Int): BitList {
        if (displacement < 0) {
            throw Exception("Cannot shift a BitList a negative number of times. Displacement: $displacement")
        } else {
            if (size <= displacement) {
                return Empty
            }

            val result = BitList(size, BitSet(size))

            for (i in displacement until size) {
                result.content[i - displacement] = content[i]
            }

            return result
        }
    }

    /**
     * Moves the bits to the right removing those bits that leave the list and setting 0s in the new positions.
     */
    fun rightShift(displacement: Int): BitList {
        if (displacement < 0) {
            throw Exception("Cannot shift a BitList a negative number of times. Displacement: $displacement")
        } else {
            if (size <= displacement) {
                return Empty
            }

            val result = BitList(size, BitSet(size))

            for (i in 0 until size - displacement) {
                result.content[displacement + i] = content[i]
            }

            return result
        }
    }

    /**
     * Rotates the bits to the left.
     */
    fun leftRotate(displacement: Int): BitList {
        if (displacement < 0) {
            throw Exception("Cannot shift a BitList a negative number of times. Displacement: $displacement")
        } else {
            val movement = displacement % size

            if (movement == 0) {
                return this
            }

            val result = BitList(size, BitSet(size))

            val end = size - movement
            for (i in 0 until end) {
                result.content[i] = content[i + movement]
            }

            for (i in end until size) {
                result.content[i] = content[i - end]
            }

            return result
        }
    }

    /**
     * Rotates the bits to the right.
     */
    fun rightRotate(displacement: Int): BitList {
        if (displacement < 0) {
            throw Exception("Cannot shift a BitList a negative number of times. Displacement: $displacement")
        } else {
            val movement = displacement % size

            if (movement == 0) {
                return this
            }

            val result = BitList(size, BitSet(size))

            val start = size - movement
            for (i in start until size) {
                result.content[i - start] = content[i]
            }

            for (i in 0 until start) {
                result.content[i + movement] = content[i]
            }

            return result
        }
    }

    /**
     * Clones the bitList returning a new value.
     */
    private fun clone() = BitList(size, content.clone() as BitSet)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun iterator(): Iterator<Boolean> {
        val sequence = sequence {
            for (i in 0 until size) {
                yield(content[i])
            }
        }

        return sequence.iterator()
    }

    override fun toString(): String {
        val result = StringBuilder()
        when {
            size % 4 == 0 -> {
                result.append(BitlistNode.hexadecimalPrefix)
                result.append(BitlistNode.startToken)

                for (i in 0 until size step 4) {
                    var digit = 0

                    for (j in 0 until 4) {
                        digit = digit.shl(1)

                        if (content[i + j]) {
                            digit += 1
                        }
                    }

                    result.append(digit.toString(16))
                }
            }
            size % 3 == 0 -> {
                result.append(BitlistNode.octalPrefix)
                result.append(BitlistNode.startToken)

                for (i in 0 until size step 3) {
                    var digit = 0

                    for (j in 0 until 3) {
                        digit = digit.shl(1)

                        if (content[i + j]) {
                            digit += 1
                        }
                    }

                    result.append(digit.toString(8))
                }
            }
            else -> {
                result.append(BitlistNode.binaryPrefix)
                result.append(BitlistNode.startToken)

                for (i in 0 until size) {
                    result.append(if (content[i]) {
                        '1'
                    } else {
                        '0'
                    })
                }
            }
        }

        result.append(BitlistNode.endToken)
        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitList

        if (size != other.size) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + content.hashCode()
        return result
    }

    // STATIC -----------------------------------------------------------------

    companion object {
        /**
         * An [BitList] with no bits.
         */
        val Empty = BitList(0, BitSet())

        // CONSTRUCTORS -----------------------------------------------------------

        /**
         * Creates a new [BitList] with the specified content.
         */
        fun new(content: ByteArray) = BitList(content.size * 8, BitSet.valueOf(content.map {
            it.reverseBits()
        }.toByteArray()))

        /**
         * Creates a new [BitList] with the specified size.
         */
        fun new(size: Int) = if (size < 0) {
            throw Exception("Cannot create a BitList with a size lower than 0. Size: $size")
        } else {
            BitList(size, BitSet(size))
        }
    }
}
