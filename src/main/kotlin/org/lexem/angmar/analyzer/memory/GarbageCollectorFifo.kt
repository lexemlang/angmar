package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*

/**
 * An fifo made with intervals and optimized for garbage collection.
 */
internal class GarbageCollectorFifo : Iterable<Int> {
    private val deadRanges = mutableListOf<IntegerRange>()
    private val restRangesToProcess = mutableListOf<IntegerRange>()

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Builds a new [GarbageCollectorFifo] from a memory bounds.
     */
    constructor(size: Int) {
        if (size < 0) {
            throw AngmarException("The size cannot be negative")
        }

        if (size > 0) {
            deadRanges.add(IntegerRange.new(0, size - 1))
        }
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Push a reference to process only if there is not already processed.
     */
    fun push(position: Int) {
        if (contains(position, deadRanges)) {
            sub(position, deadRanges)
            add(position, restRangesToProcess)
        }
    }

    /**
     * Pops the next reference to be processed.
     */
    fun pop(): Int? {
        if (restRangesToProcess.isEmpty()) {
            return null
        }

        val point = restRangesToProcess.first().from
        sub(point, restRangesToProcess)

        return point
    }

    /**
     * Adds a point to the specified interval.
     */
    private fun add(point: Int, ranges: MutableList<IntegerRange>) {
        var index = binarySearch(point, ranges)
        if (index >= 0) {
            return
        }

        // Fix the index.
        index = -(index + 1)

        val prevRange = ranges.getOrNull(index - 1)
        val nextRange = ranges.getOrNull(index)

        if (prevRange != null) {
            if (nextRange != null) {
                if (prevRange.to == point - 1) {
                    if (point + 1 == nextRange.from) {
                        ranges[index - 1] = IntegerRange.new(prevRange.from, nextRange.to)
                        ranges.removeAt(index)
                    } else {
                        ranges[index - 1] = IntegerRange.new(prevRange.from, point)
                    }
                } else {
                    if (point + 1 == nextRange.from) {
                        ranges[index] = IntegerRange.new(point, nextRange.to)
                    } else {
                        ranges.add(index, IntegerRange.new(point))
                    }
                }
            } else {
                if (prevRange.to == point - 1) {
                    ranges[index - 1] = IntegerRange.new(prevRange.from, point)
                } else {
                    ranges.add(index, IntegerRange.new(point))
                }
            }
        } else {
            if (nextRange != null) {
                if (point + 1 == nextRange.from) {
                    ranges[index] = IntegerRange.new(point, nextRange.to)
                } else {
                    ranges.add(index, IntegerRange.new(point))
                }
            } else {
                ranges.add(IntegerRange.new(point))
            }
        }
    }

    /**
     * Removes a point from the specified interval.
     */
    private fun sub(point: Int, ranges: MutableList<IntegerRange>) {
        when {
            ranges.isEmpty() -> return
            point < ranges.first().from -> return
            ranges.last().to < point -> return
        }

        val index = binarySearch(point, ranges)
        if (index < 0) {
            return
        }

        val range = ranges[index]

        when (point) {
            range.from -> {
                if (point == range.to) {
                    ranges.removeAt(index)
                    return
                }

                ranges[index] = IntegerRange.new(range.from + 1, range.to)
            }
            range.to -> {
                ranges[index] = IntegerRange.new(range.from, range.to - 1)
            }
            else -> {
                ranges[index] = IntegerRange.new(range.from, point - 1)
                ranges.add(index + 1, IntegerRange.new(point + 1, range.to))
            }
        }
    }

    /**
     * Returns the index of the range that contains the point or the inverted insertion point `(-insertion point - 1)` otherwise.
     * The insertion point is defined as the index at which the element should be inserted,
     * so that the list (or the specified subrange of list) still remains sorted.
     */
    private fun binarySearch(point: Int, ranges: MutableList<IntegerRange>): Int {
        return ranges.binarySearch {
            when {
                point < it.from -> 1
                point > it.to -> -1
                else -> 0
            }
        }
    }

    /**
     * Checks if a point is inside the specified interval.
     */
    private fun contains(point: Int, ranges: MutableList<IntegerRange>) = binarySearch(point, ranges) >= 0

    /**
     * Whether this [GarbageCollectorFifo] is empty or not.
     */
    val isEmpty get() = deadRanges.isEmpty()

    /**
     * Whether this [GarbageCollectorFifo] is empty or not.
     */
    val isNotEmpty get() = deadRanges.isNotEmpty()

    // INHERIT METHODS --------------------------------------------------------

    /**
     * [GarbageCollectorFifo]'s iterator over its points.
     */
    override fun iterator() = GarbageCollectorFifoIterator(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GarbageCollectorFifo

        if (deadRanges != other.deadRanges) return false
        if (restRangesToProcess != other.restRangesToProcess) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deadRanges.hashCode()
        result = 31 * result + restRangesToProcess.hashCode()
        return result
    }

    override fun toString() = StringBuilder().apply {
        append("Dead")
        if (deadRanges.isEmpty()) {
            append("[]")
        } else {
            append(deadRanges.joinToString(" U "))
        }

        append(" ToProcess")
        if (restRangesToProcess.isEmpty()) {
            append("[]")
        } else {
            append(restRangesToProcess.joinToString(" U "))
        }
    }.toString()

    // STATIC -----------------------------------------------------------------

    /**
     * [GarbageCollectorFifo]'s iterator over the dead points.
     */
    class GarbageCollectorFifoIterator internal constructor(interval: GarbageCollectorFifo) : Iterator<Int> {
        private val rangeIterator = interval.deadRanges.iterator()
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
}
