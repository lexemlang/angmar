package org.lexem.angmar.data

import org.lexem.angmar.config.*
import java.util.*
import kotlin.math.*

/**
 * A complex interval of integer numbers.
 */
class IntegerInterval private constructor() : Iterable<Int> {
    private val ranges = mutableListOf<IntegerRange>()

    // METHODS ----------------------------------------------------------------

    /**
     * Returns a new [IntegerInterval] with the result of adding this [IntegerInterval] with the specified point.
     */
    operator fun plus(point: Int) = plus(IntegerRange.new(point))

    /**
     * Returns a new [IntegerInterval] with the result of adding this [IntegerInterval] with the specified [IntegerRange].
     */
    operator fun plus(range: IntegerRange) = plus(new(range))

    /**
     * Returns a new [IntegerInterval] with the result of adding this [IntegerInterval] with the specified [IntegerInterval].
     */
    operator fun plus(itv: IntegerInterval): IntegerInterval {
        when {
            isEmpty -> return itv
            itv.isEmpty -> return this
            itv.ranges.last().to + 1L < ranges.first().from -> {
                val result = new()
                result.ranges.addAll(itv.ranges)
                result.ranges.addAll(ranges)
                return result
            }
            ranges.last().to + 1L < itv.ranges.first().from -> {
                val result = new()
                result.ranges.addAll(ranges)
                result.ranges.addAll(itv.ranges)
                return result
            }
        }

        var indexThis = binarySearch(itv.ranges.first().from)
        var indexItv = 0
        val result = IntegerInterval()

        if (indexThis < 0) {
            indexThis = -(indexThis + 1)
        }

        result.ranges.addAll(ranges.slice(0 until indexThis))

        loop@ while (indexThis <= ranges.lastIndex && indexItv <= itv.ranges.lastIndex) {
            when {
                ranges[indexThis].to + 1L < itv.ranges[indexItv].from -> {
                    normalizedAddition(ranges[indexThis], result)
                    indexThis += 1

                    if (indexThis > ranges.lastIndex) {
                        if (indexItv <= itv.ranges.lastIndex) {
                            normalizedAddition(itv.ranges[indexItv], result)
                            indexItv += 1
                        }
                        break@loop
                    }
                }
                itv.ranges[indexItv].to + 1L < ranges[indexThis].from -> {
                    normalizedAddition(itv.ranges[indexItv], result)
                    indexItv += 1

                    if (indexItv > itv.ranges.lastIndex) {
                        if (indexThis <= ranges.lastIndex) {
                            normalizedAddition(ranges[indexThis], result)
                            indexThis += 1
                        }
                        break@loop
                    }
                }
                else -> {
                    val thisRange = ranges[indexThis]
                    val itvRange = itv.ranges[indexItv]
                    val from = min(thisRange.from, itvRange.from)
                    var to = max(thisRange.to, itvRange.to)
                    indexThis += 1
                    indexItv += 1

                    var continueIteration = true
                    while (continueIteration) {
                        continueIteration = false

                        while (indexThis <= ranges.lastIndex && to + 1L >= ranges[indexThis].from) {
                            to = max(to, ranges[indexThis].to)
                            indexThis += 1
                            continueIteration = true
                        }

                        while (indexItv <= itv.ranges.lastIndex && to + 1L >= itv.ranges[indexItv].from) {
                            to = max(to, itv.ranges[indexItv].to)
                            indexItv += 1
                            continueIteration = true
                        }
                    }

                    normalizedAddition(IntegerRange.new(from, to), result)

                    if (indexThis > ranges.lastIndex) {
                        break@loop
                    }

                    if (indexItv > itv.ranges.lastIndex) {
                        break@loop
                    }
                }
            }
        }

        if (indexThis <= ranges.lastIndex) {
            normalizedAddition(ranges.slice(indexThis until ranges.size), result)
        }

        if (indexItv <= itv.ranges.lastIndex) {
            normalizedAddition(itv.ranges.slice(indexItv until itv.ranges.size), result)
        }

        return result
    }

    /**
     * Returns a new [IntegerInterval] with the result of subtracting the specified point from this [IntegerInterval].
     */
    operator fun minus(point: Int) = minus(IntegerRange.new(point))

    /**
     * Returns a new [IntegerInterval] with the result of subtracting the specified [IntegerRange] from this [IntegerInterval].
     */
    operator fun minus(range: IntegerRange) = minus(new(range))

    /**
     * Returns a new [IntegerInterval] with the result of subtracting the specified [IntegerInterval] from this [IntegerInterval].
     */
    operator fun minus(itv: IntegerInterval): IntegerInterval {
        when {
            isEmpty -> return Empty
            itv.isEmpty -> return this
            itv.ranges.last().to < ranges.first().from -> return this
            ranges.last().to < itv.ranges.first().from -> return this
        }

        var indexThis = binarySearch(itv.ranges.first().from)
        var indexItv = 0

        if (indexThis < 0) {
            indexThis = -(indexThis + 1)
        }

        val result = IntegerInterval()
        result.ranges.addAll(ranges.slice(0 until indexThis))

        var thisFrom = ranges[indexThis].from
        var thisTo = ranges[indexThis].to

        loop@ while (indexThis <= ranges.lastIndex && indexItv <= itv.ranges.lastIndex) {
            when {
                thisTo < itv.ranges[indexItv].from -> {
                    result.ranges.add(IntegerRange.new(thisFrom, thisTo))
                    indexThis += 1

                    if (indexThis > ranges.lastIndex) {
                        break@loop
                    }

                    thisFrom = ranges[indexThis].from
                    thisTo = ranges[indexThis].to
                }
                itv.ranges[indexItv].to < thisFrom -> {
                    indexItv += 1
                }
                else -> {
                    if (thisFrom < itv.ranges[indexItv].from) {
                        result.ranges.add(IntegerRange.new(thisFrom, itv.ranges[indexItv].from - 1))
                    }

                    if (thisTo <= itv.ranges[indexItv].to) {
                        indexThis += 1

                        if (indexThis > ranges.lastIndex) {
                            break@loop
                        }

                        thisFrom = ranges[indexThis].from
                        thisTo = ranges[indexThis].to
                    } else {
                        thisFrom = itv.ranges[indexItv].to + 1
                        indexItv += 1
                    }
                }
            }
        }

        if (indexItv > itv.ranges.lastIndex) {
            result.ranges.add(IntegerRange.new(thisFrom, thisTo))
            result.ranges.addAll(ranges.slice(indexThis + 1 until ranges.size))
        }

        return result
    }

    /**
     * Returns a new [IntegerInterval] with the result of getting the common section between the specified point and this [IntegerInterval].
     */
    fun common(point: Int) = when {
        isEmpty || binarySearch(point) < 0 -> Empty
        else -> new(IntegerRange.new(point))
    }

    /**
     * Returns a new [IntegerInterval] with the result of getting the common section between the specified [IntegerRange] and this [IntegerInterval].
     */
    fun common(range: IntegerRange) = common(new(range))

    /**
     * Returns a new [IntegerInterval] with the result of getting the common section between the specified [IntegerInterval] and this [IntegerInterval].
     */
    fun common(itv: IntegerInterval): IntegerInterval {
        when {
            isEmpty -> return Empty
            itv.isEmpty -> return Empty
            itv.ranges.last().to < ranges.first().from -> return Empty
            ranges.last().to < itv.ranges.first().from -> return Empty
        }

        var indexThis = binarySearch(itv.ranges.first().from)
        var indexItv = 0
        val result = IntegerInterval()

        if (indexThis < 0) {
            indexThis = -(indexThis + 1)
        }

        loop@ while (indexThis <= ranges.lastIndex && indexItv <= itv.ranges.lastIndex) {
            when {
                ranges[indexThis].to < itv.ranges[indexItv].from -> {
                    indexThis += 1
                }
                itv.ranges[indexItv].to < ranges[indexThis].from -> {
                    indexItv += 1
                }
                else -> {
                    result.ranges.add(IntegerRange.new(max(ranges[indexThis].from, itv.ranges[indexItv].from),
                            min(ranges[indexThis].to, itv.ranges[indexItv].to)))

                    if (ranges[indexThis].to <= itv.ranges[indexItv].to) {
                        indexThis += 1
                    } else {
                        indexItv += 1
                    }
                }
            }
        }

        return result
    }

    /**
     * Returns a new [IntegerInterval] with the result of getting the not common section between the specified point and this [IntegerInterval].
     */
    fun notCommon(point: Int) = when {
        isEmpty -> new(IntegerRange.new(point))
        binarySearch(point) < 0 -> plus(point)
        else -> minus(point)
    }

    /**
     * Returns a new [IntegerInterval] with the result of getting the not common section between the specified [IntegerRange] and this [IntegerInterval].
     */
    fun notCommon(range: IntegerRange) = notCommon(new(range))

    /**
     * Returns a new [IntegerInterval] with the result of getting the not common section between the specified [IntegerInterval] and this [IntegerInterval].
     */
    fun notCommon(itv: IntegerInterval): IntegerInterval {
        when {
            isEmpty -> return itv
            itv.isEmpty -> return this
            itv.ranges.last().to < ranges.first().from -> {
                val result = new()
                result.ranges.addAll(itv.ranges)
                normalizedAddition(ranges, result)
                return result
            }
            ranges.last().to < itv.ranges.first().from -> {
                val result = new()
                result.ranges.addAll(ranges)
                normalizedAddition(itv.ranges, result)
                return result
            }
        }

        var indexThis = binarySearch(itv.ranges.first().from)
        var indexItv = 0

        if (indexThis < 0) {
            indexThis = -(indexThis + 1)
        }

        val result = IntegerInterval()
        result.ranges.addAll(ranges.slice(0 until indexThis))

        var thisFrom = ranges[indexThis].from
        var thisTo = ranges[indexThis].to
        var itvFrom = itv.ranges[indexItv].from
        var itvTo = itv.ranges[indexItv].to

        loop@ while (indexThis <= ranges.lastIndex && indexItv <= itv.ranges.lastIndex) {
            when {
                thisTo < itvFrom -> {
                    normalizedAddition(IntegerRange.new(thisFrom, thisTo), result)
                    indexThis += 1

                    if (indexThis > ranges.lastIndex) {
                        break@loop
                    }

                    thisFrom = ranges[indexThis].from
                    thisTo = ranges[indexThis].to
                }
                itvTo < thisFrom -> {
                    normalizedAddition(IntegerRange.new(itvFrom, itvTo), result)
                    indexItv += 1

                    if (indexItv > itv.ranges.lastIndex) {
                        break@loop
                    }

                    itvFrom = itv.ranges[indexItv].from
                    itvTo = itv.ranges[indexItv].to
                }
                else -> {
                    if (thisFrom != itvFrom) {
                        normalizedAddition(IntegerRange.new(min(thisFrom, itvFrom), max(thisFrom, itvFrom) - 1), result)
                    }

                    if (thisTo < itvTo) {
                        itvFrom = thisTo + 1
                        indexThis += 1

                        if (indexThis > ranges.lastIndex) {
                            break@loop
                        }

                        thisFrom = ranges[indexThis].from
                        thisTo = ranges[indexThis].to
                    } else if (itvTo < thisTo) {
                        thisFrom = itvTo + 1
                        indexItv += 1

                        if (indexItv > itv.ranges.lastIndex) {
                            break@loop
                        }

                        itvFrom = itv.ranges[indexItv].from
                        itvTo = itv.ranges[indexItv].to
                    } else {
                        indexThis += 1
                        indexItv += 1

                        if (indexThis > ranges.lastIndex) {
                            if (indexItv <= itv.ranges.lastIndex) {
                                itvFrom = itv.ranges[indexItv].from
                                itvTo = itv.ranges[indexItv].to
                            }
                            break@loop
                        }

                        if (indexItv > itv.ranges.lastIndex) {
                            thisFrom = ranges[indexThis].from
                            thisTo = ranges[indexThis].to
                            break@loop
                        }

                        thisFrom = ranges[indexThis].from
                        thisTo = ranges[indexThis].to
                        itvFrom = itv.ranges[indexItv].from
                        itvTo = itv.ranges[indexItv].to
                    }
                }
            }
        }

        if (indexThis <= ranges.lastIndex) {
            normalizedAddition(IntegerRange.new(thisFrom, thisTo), result)
            normalizedAddition(ranges.slice(indexThis + 1 until ranges.size), result)
        }
        if (indexItv <= itv.ranges.lastIndex) {
            normalizedAddition(IntegerRange.new(itvFrom, itvTo), result)
            normalizedAddition(itv.ranges.slice(indexItv + 1 until itv.ranges.size), result)
        }

        return result
    }

    /**
     * Returns the reversed [IntegerInterval] of the current.
     */
    operator fun not() = when {
        isEmpty -> Full
        this == Full -> Empty
        else -> notCommon(Full)
    }

    /**
     * Returns the reversed [IntegerInterval] of the current cropping the result to the Unicode range.
     */
    fun unicodeNot() = (!this).common(Unicode)

    /**
     * Returns the index of the range that contains the point or the inverted insertion point `(-insertion point - 1)` otherwise.
     * The insertion point is defined as the index at which the element should be inserted,
     * so that the list (or the specified subrange of list) still remains sorted.
     */
    fun binarySearch(point: Int): Int {
        return ranges.binarySearch {
            when {
                point < it.from -> 1
                point > it.to -> -1
                else -> 0
            }
        }
    }

    /**
     * The number of values in the [IntegerInterval].
     */
    val pointCount: Long by lazy {
        ranges.fold(0L) { acc, range ->
            acc + range.pointCount
        }
    }

    /**
     * The number of ranges in the [IntegerInterval].
     */
    val rangeCount get() = ranges.size

    /**
     * Returns the point at the specified index.
     */
    operator fun get(index: Int): Int? {
        if (index < 0) {
            return null
        }

        var currentIndex = index.toLong()
        for (range in ranges) {
            if (currentIndex >= range.pointCount) {
                currentIndex -= range.pointCount
            } else {
                return range[currentIndex.toInt()]
            }
        }

        return null
    }

    /**
     * Returns the [IntegerRange] at the specified index.
     */
    fun rangeAtOrNull(index: Int): IntegerRange? {
        return ranges.getOrNull(index)
    }

    /**
     * Checks if a point is inside this [IntegerRange].
     */
    operator fun contains(point: Int) = binarySearch(point) >= 0

    /**
     * Whether this [IntegerInterval] is empty or not.
     */
    val isEmpty get() = ranges.isEmpty()

    /**
     * The first point of the [IntegerInterval].
     */
    val firstPoint
        get() = if (!isEmpty) {
            ranges.first().from
        } else {
            null
        }

    /**
     * The last point of the [IntegerInterval].
     */
    val lastPoint
        get() = if (!isEmpty) {
            ranges.last().to
        } else {
            null
        }

    // INHERIT METHODS --------------------------------------------------------

    /**
     * [IntegerInterval]'s iterator over its points.
     */
    override fun iterator() = IntervalIterator(this)

    /**
     * [IntegerInterval]'s iterator over its ranges.
     */
    fun rangeIterator() = ranges.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntegerInterval

        if (ranges != other.ranges) return false

        return true
    }

    override fun hashCode(): Int {
        return ranges.hashCode()
    }

    override fun toString() = if (isEmpty) {
        "[]"
    } else {
        ranges.joinToString(" U ")
    }

    /**
     * ToString with hexadecimal representation of the bounds.
     */
    fun toHexString() = if (isEmpty) {
        "[]"
    } else {
        ranges.joinToString(" U ") {
            it.toHexString()
        }
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
     * [IntegerInterval]'s iterator over its points.
     */
    class IntervalIterator internal constructor(interval: IntegerInterval) : Iterator<Int> {
        private val rangeIterator = interval.rangeIterator()
        private var pointIterator = if (rangeIterator.hasNext()) {
            rangeIterator.next().iterator()
        } else {
            null
        }

        override fun hasNext() = pointIterator != null

        override fun next(): Int {
            val res = pointIterator!!.next()

            if (!pointIterator!!.hasNext()) {
                if (rangeIterator.hasNext()) {
                    pointIterator = rangeIterator.next().iterator()
                } else {
                    pointIterator = null
                }
            }

            return res
        }
    }

    companion object {
        private val instances = Stack<IntegerInterval>()

        /**
         * An [IntegerInterval] with no points.
         */
        val Empty = IntegerInterval()

        /**
         * An [IntegerInterval] with all points.
         */
        val Full = new(IntegerRange.Full)

        /**
         * An [IntegerInterval] with all Unicode points.
         */
        val Unicode = new(IntegerRange.Unicode)

        // CONSTRUCTORS -----------------------------------------------------------

        private fun new() = if (instances.size > 0) {
            instances.pop()!!
        } else {
            IntegerInterval()
        }

        /**
         * Builds a new [IntegerInterval] from a [IntegerRange].
         */
        fun new(range: IntegerRange): IntegerInterval {
            val result = new()
            result.ranges.add(range)

            return result
        }

        // METHODS ------------------------------------------------------------

        /**
         * Adds a [IntegerRange] inside a mutable [IntegerInterval] combining it with the previous [IntegerRange].
         */
        private fun normalizedAddition(range: IntegerRange, interval: IntegerInterval) {
            if (!interval.isEmpty && interval.ranges.last().to + 1 >= range.from) {
                interval.ranges[interval.ranges.lastIndex] = IntegerRange.new(interval.ranges.last().from, range.to)
                return
            }

            interval.ranges.add(range)
        }

        /**
         * Adds a list of [IntegerRange]s inside a mutable [IntegerInterval] combining it with the previous [IntegerRange].
         */
        private fun normalizedAddition(ranges: List<IntegerRange>, interval: IntegerInterval) {
            if (ranges.isEmpty()) {
                return
            }

            normalizedAddition(ranges[0], interval)
            interval.ranges.addAll(ranges.slice(1 until ranges.size))
        }
    }
}
