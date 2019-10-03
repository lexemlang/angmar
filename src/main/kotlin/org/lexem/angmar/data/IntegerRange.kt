package org.lexem.angmar.data

import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import java.util.*

/**
 * An inclusive range of natural numbers.
 */
class IntegerRange private constructor() : Iterable<Int>, Comparable<Int> {
    var from = 0
        private set
    var to = 0
        private set

    // METHODS ----------------------------------------------------------------

    /**
     * The number of values in the [IntegerRange].
     */
    val pointCount: Long
        get() = to - from + 1L

    /**
     * Returns the point at the specified index.
     */
    operator fun get(index: Int): Int? {
        if (index < 0 || index >= pointCount) {
            return null
        }

        return from + index
    }

    /**
     * Checks if a point is inside this [IntegerRange].
     */
    operator fun contains(point: Int) = point in from..to

    /**
     * Checks if both [IntegerRange]s are near between them, i.e. at most at one point of distance.
     * E.g. [2, 3] is near to [0, 1] but not to [5, 6].
     */
    fun isNearTo(range: IntegerRange) = when {
        to + 1 < range.from -> false
        range.to + 1 < from -> false
        else -> true
    }

    // INHERIT METHODS --------------------------------------------------------

    /**
     * Compares this [IntegerRange] with a point returning:
     * -1 if Range < Point
     *  0 if Point inside Range
     *  1 if Point < Range
     */
    override fun compareTo(other: Int) = when {
        other in this -> 0
        other < from -> 1
        else -> -1
    }

    override fun iterator(): Iterator<Int> = RangeIterator(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntegerRange

        if (from != other.from) return false
        if (to != other.to) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }

    override fun toString() = if (from == to) {
        "[$from]"
    } else {
        "[$from, $to]"
    }

    /**
     * ToString with hexadecimal representation of the bounds.
     */
    fun toHexString() = if (from == to) {
        "[${from.toString(16)}]"
    } else {
        "[${from.toString(16)}, ${to.toString(16)}]"
    }

    /**
     * Destroys the range to be reused.
     * MUST ONLY BE CALLED WHENEVER THE CELL IS REMOVED FROM MEMORY.
     */
    fun destroy() {
        if (instances.size < Consts.Memory.maxPoolSize) {
            instances.add(this)
        }
    }

    // STATIC -----------------------------------------------------------------

    /**
     * [IntegerRange]'s iterator over its points.
     */
    class RangeIterator internal constructor(range: IntegerRange) : Iterator<Int> {
        private val range = range.from..range.to
        private var current = range.from

        override fun hasNext(): Boolean = current in range

        override fun next(): Int {
            val res = current
            current += 1
            return res
        }
    }

    companion object {
        private val instances = Stack<IntegerRange>()

        /**
         * An [IntegerRange] with all points.
         */
        val Full = new(0, Int.MAX_VALUE)

        /**
         * An [IntegerRange] with all Unicode points.
         */
        val Unicode = new(0, 0x10FFFF)

        // CONSTRUCTORS -------------------------------------------------------

        /**
         * Builds a new [IntegerRange] with one point.
         */
        fun new(value: Int) = new(value, value)

        /**
         * Builds a new [IntegerRange] from two points.
         */
        fun new(from: Int, to: Int): IntegerRange {
            if (from > to) {
                throw AngmarException("The 'from' parameter cannot be greater or equal than the 'to' parameter.")
            }

            if (from < 0) {
                throw AngmarException("The 'from' parameter cannot be negative.")
            }

            val instance = if (instances.size > 0) {
                instances.pop()!!
            } else {
                IntegerRange()
            }

            instance.from = from
            instance.to = to

            return instance
        }
    }
}
