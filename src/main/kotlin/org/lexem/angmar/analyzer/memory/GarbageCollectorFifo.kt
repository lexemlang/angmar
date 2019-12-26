package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.errors.*

/**
 * An fifo made with intervals and optimized for garbage collection.
 */
internal class GarbageCollectorFifo : Iterable<Long> {
    private val deadRanges = mutableListOf<LongRange>()
    private val restRangesToProcess = mutableListOf<LongRange>()

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Builds a new [GarbageCollectorFifo] from a memory bounds.
     */
    constructor(size: Int) {
        if (size < 0) {
            throw AngmarException("The size cannot be negative")
        }

        if (size > 0) {
            deadRanges.add(0L until size)
        }
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Push a reference to process only if there is not already processed.
     */
    fun push(position: Long) {
        if (contains(position, deadRanges)) {
            sub(position, deadRanges)
            add(position, restRangesToProcess)
        }
    }

    /**
     * Pops the next reference to be processed.
     */
    fun pop(): Long? {
        if (restRangesToProcess.isEmpty()) {
            return null
        }

        val point = restRangesToProcess.first().first
        sub(point, restRangesToProcess)

        return point
    }

    /**
     * Adds a point to the specified interval.
     */
    private fun add(point: Long, ranges: MutableList<LongRange>) {
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
                if (prevRange.last == point - 1) {
                    if (point + 1 == nextRange.first) {
                        ranges[index - 1] = prevRange.first..nextRange.last
                        ranges.removeAt(index)
                    } else {
                        ranges[index - 1] = prevRange.first..point
                    }
                } else {
                    if (point + 1 == nextRange.first) {
                        ranges[index] = point..nextRange.last
                    } else {
                        ranges.add(index, point..point)
                    }
                }
            } else {
                if (prevRange.last == point - 1) {
                    ranges[index - 1] = prevRange.first..point
                } else {
                    ranges.add(index, point..point)
                }
            }
        } else {
            if (nextRange != null) {
                if (point + 1 == nextRange.first) {
                    ranges[index] = point..nextRange.last
                } else {
                    ranges.add(index, point..point)
                }
            } else {
                ranges.add(point..point)
            }
        }
    }

    /**
     * Removes a point from the specified interval.
     */
    private fun sub(point: Long, ranges: MutableList<LongRange>) {
        when {
            ranges.isEmpty() -> return
            point < ranges.first().first -> return
            ranges.last().last < point -> return
        }

        val index = binarySearch(point, ranges)
        if (index < 0) {
            return
        }

        val range = ranges[index]

        when (point) {
            range.first -> {
                if (point == range.last) {
                    ranges.removeAt(index)
                    return
                }

                ranges[index] = range.first + 1..range.last
            }
            range.last -> {
                ranges[index] = range.first until range.last
            }
            else -> {
                ranges[index] = range.first until point
                ranges.add(index + 1, point + 1..range.last)
            }
        }
    }

    /**
     * Returns the index of the range that contains the point or the inverted insertion point `(-insertion point - 1)` otherwise.
     * The insertion point is defined as the index at which the element should be inserted,
     * so that the list (or the specified subrange of list) still remains sorted.
     */
    private fun binarySearch(point: Long, ranges: MutableList<LongRange>): Int {
        return ranges.binarySearch {
            when {
                point < it.first -> 1
                point > it.last -> -1
                else -> 0
            }
        }
    }

    /**
     * Checks if a point is inside the specified interval.
     */
    private fun contains(point: Long, ranges: MutableList<LongRange>) = binarySearch(point, ranges) >= 0

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
    class GarbageCollectorFifoIterator internal constructor(interval: GarbageCollectorFifo) : Iterator<Long> {
        private val rangeIterator = interval.deadRanges.iterator()
        private var pointIterator = if (rangeIterator.hasNext()) {
            rangeIterator.next().iterator()
        } else {
            null
        }

        override fun hasNext() = pointIterator != null

        override fun next(): Long {
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
