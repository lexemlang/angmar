package org.lexem.angmar.data

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import java.lang.Integer.*
import java.util.stream.*
import kotlin.streams.*

internal class IntegerIntervalTest {
    companion object {
        private val complexRange1 = IntegerRange.new(0, 50)
        private val complexRange2 = IntegerRange.new(100, 150)
        private val complexRange3 = IntegerRange.new(200, 250)
        private val complexRange4 = IntegerRange.new(252, 300)
        private val complexRange5 = IntegerRange.new(350)
        private var complexItv =
                IntegerInterval.Empty + complexRange1 + complexRange2 + complexRange3 + complexRange4 + complexRange5

        // New
        private val simpleRanges = mutableListOf<IntegerRange>()
        private val simpleIntervals = mutableListOf<IntegerInterval>()

        init {
            // Ranges
            simpleRanges += IntegerRange.Full
            simpleRanges += IntegerRange.new(0, 500)
            simpleRanges += IntegerRange.new(499, 600)
            val pivot = IntegerRange.new(500, 600)
            simpleRanges += pivot
            simpleRanges += IntegerRange.new(501, 600)
            simpleRanges += IntegerRange.new(700, 900)
            simpleRanges += IntegerRange.new(899, Int.MAX_VALUE)
            simpleRanges += IntegerRange.new(900, Int.MAX_VALUE)
            simpleRanges += IntegerRange.new(901, Int.MAX_VALUE)

            // Intervals
            simpleIntervals += IntegerInterval.Empty

            for (i in simpleRanges) {
                simpleIntervals += IntegerInterval.new(i)
            }

            simpleIntervals += IntegerInterval.new(
                    IntegerRange.new(pivot.from - 100, pivot.from - 1)) + IntegerRange.new(pivot.to + 1, pivot.to + 100)


            simpleIntervals += IntegerInterval.new(IntegerRange.new(pivot.from, pivot.from + 10)) + IntegerRange.new(
                    pivot.from + 20, pivot.to + 30) + IntegerRange.new(pivot.from + 40, pivot.to + 50)
        }

        @JvmStatic
        fun provideRangePoint(): Stream<Arguments> {
            val result = sequence {
                for (range in simpleRanges) {
                    val interval = IntegerInterval.new(range)

                    if (range.from - 1 >= 0) {
                        yield(Arguments.of(interval, range.from - 1))
                    }

                    yield(Arguments.of(interval, range.from))
                    yield(Arguments.of(interval, ((range.from.toLong() + range.to) / 2).toInt()))
                    yield(Arguments.of(interval, range.to))

                    if (range.to + 1L <= Int.MAX_VALUE) {
                        yield(Arguments.of(interval, range.to + 1))
                    }
                }
            }

            return result.asStream()
        }

        @JvmStatic
        fun provideIntervalRange(): Stream<Arguments> {
            val result = sequence {
                for (left in simpleIntervals) {
                    for (right in simpleRanges) {
                        yield(Arguments.of(left, right))
                    }
                }
            }

            return result.asStream()
        }

        @JvmStatic
        fun provideIntervalInterval(): Stream<Arguments> {
            val result = sequence {
                for (left in simpleIntervals) {
                    for (right in simpleIntervals) {
                        yield(Arguments.of(left, right))
                    }
                }
            }

            return result.asStream()
        }

        @JvmStatic
        fun provideIntervals(): Stream<Arguments> {
            val result = sequence {
                for (interval in simpleIntervals) {
                    yield(Arguments.of(interval))
                }
            }

            return result.asStream()
        }
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun constructor() {
        var itv = IntegerInterval.new(complexRange1)
        Assertions.assertNotNull(itv)
        Assertions.assertEquals(complexRange1.from, itv.firstPoint)
        Assertions.assertEquals(complexRange1.to, itv.lastPoint)
        Assertions.assertEquals(1, itv.rangeCount)

        itv = IntegerInterval.new(IntegerRange.new(complexRange2.from))
        Assertions.assertNotNull(itv)
        Assertions.assertEquals(complexRange2.from, itv.firstPoint)
        Assertions.assertEquals(complexRange2.from, itv.lastPoint)
        Assertions.assertEquals(1, itv.rangeCount)
    }

    // PLUS -------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideRangePoint")
    fun `test plus point`(interval: IntegerInterval, point: Int) {
        val expected = manualPlus(interval, IntegerInterval.new(IntegerRange.new(point)))
        val result = interval + point
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideIntervalRange")
    fun `test plus range`(interval: IntegerInterval, range: IntegerRange) {
        val expected = manualPlus(interval, IntegerInterval.new(range))
        val result = interval + range
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideIntervalInterval")
    fun `test plus interval`(left: IntegerInterval, right: IntegerInterval) {
        val expected = manualPlus(left, right)
        val result = left + right
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    private fun manualPlus(itv1: IntegerInterval, itv2: IntegerInterval): IntegerInterval {
        val ranges = mutableListOf<IntegerRange>()
        ranges.addAll(itv1.rangeIterator().asSequence())
        ranges.addAll(itv2.rangeIterator().asSequence())

        if (ranges.isEmpty()) {
            return IntegerInterval.Empty
        }

        ranges.sortBy { it.from }
        val newRanges = mutableListOf<IntegerRange>()
        var last = ranges[0]

        for (i in 1 until ranges.size) {
            val current = ranges[i]

            if (last.to < current.from - 1) {
                newRanges.add(last)
                last = current
            } else {
                last = IntegerRange.new(last.from, max(last.to, current.to))
            }
        }

        newRanges.add(last)

        var result = IntegerInterval.Empty
        newRanges.forEach { itv ->
            result += itv
        }

        return result
    }

    // MINUS -------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideRangePoint")
    fun `test minus point`(interval: IntegerInterval, point: Int) {
        val expected = manualMinus(interval, IntegerInterval.new(IntegerRange.new(point)))
        val result = interval - point
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideIntervalRange")
    fun `test minus range`(interval: IntegerInterval, range: IntegerRange) {
        val expected = manualMinus(interval, IntegerInterval.new(range))
        val result = interval - range
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideIntervalInterval")
    fun `test minus interval`(left: IntegerInterval, right: IntegerInterval) {
        val expected = manualMinus(left, right)
        val result = left - right
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    private fun manualMinus(itv1: IntegerInterval, itv2: IntegerInterval): IntegerInterval {
        var ranges = itv1.rangeIterator().asSequence().toList()
        val minus = itv2.rangeIterator().asSequence().toList()

        for (i in minus) {
            ranges = ranges.flatMap {
                if (i.from <= it.from && it.to <= i.to) {
                    return@flatMap emptyList<IntegerRange>()
                }

                if (i.to < it.from || it.to < i.from) {
                    listOf(it)
                } else {
                    val resList = mutableListOf<IntegerRange>()
                    if (it.from < i.from) {
                        resList += IntegerRange.new(it.from, i.from - 1)
                    }

                    if (i.to < it.to) {
                        resList += IntegerRange.new(i.to + 1, it.to)
                    }

                    resList
                }
            }
        }

        var result = IntegerInterval.Empty
        ranges.forEach { itv ->
            result += itv
        }

        return result
    }

    // COMMON -----------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideRangePoint")
    fun `test common point`(interval: IntegerInterval, point: Int) {
        val expected = manualCommon(interval, IntegerInterval.new(IntegerRange.new(point)))
        val result = interval.common(point)
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideIntervalRange")
    fun `test common range`(interval: IntegerInterval, range: IntegerRange) {
        val expected = manualCommon(interval, IntegerInterval.new(range))
        val result = interval.common(range)
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideIntervalInterval")
    fun `test common interval`(left: IntegerInterval, right: IntegerInterval) {
        val expected = manualCommon(left, right)
        val result = left.common(right)
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    private fun manualCommon(itv1: IntegerInterval, itv2: IntegerInterval): IntegerInterval {
        val left = manualPlus(itv1, itv2)
        val right = manualNotCommon(itv2, itv1)

        return manualMinus(left, right)
    }

    // NOT COMMON -------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideRangePoint")
    fun `test not common point`(interval: IntegerInterval, point: Int) {
        val expected = manualNotCommon(interval, IntegerInterval.new(IntegerRange.new(point)))
        val result = interval.notCommon(point)
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideIntervalRange")
    fun `test not common range`(interval: IntegerInterval, range: IntegerRange) {
        val expected = manualNotCommon(interval, IntegerInterval.new(range))
        val result = interval.notCommon(range)
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    @ParameterizedTest
    @MethodSource("provideIntervalInterval")
    fun `test not common interval`(left: IntegerInterval, right: IntegerInterval) {
        val expected = manualNotCommon(left, right)
        val result = left.notCommon(right)
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    private fun manualNotCommon(itv1: IntegerInterval, itv2: IntegerInterval): IntegerInterval {
        val left = manualMinus(itv1, itv2)
        val right = manualMinus(itv2, itv1)

        return manualPlus(left, right)
    }

    // NOT --------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideIntervals")
    fun `test not`(interval: IntegerInterval) {
        val expected = manualNot(interval)
        val result = interval.not()
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    private fun manualNot(itv1: IntegerInterval): IntegerInterval {
        return manualNotCommon(itv1, IntegerInterval.Full)
    }

    @ParameterizedTest
    @MethodSource("provideIntervals")
    fun `test unicode not`(interval: IntegerInterval) {
        val expected = manualUnicodeNot(interval)
        val result = interval.unicodeNot()
        Assertions.assertEquals(expected, result, "The result is incorrect")
    }

    private fun manualUnicodeNot(itv1: IntegerInterval): IntegerInterval {
        return manualCommon(manualNotCommon(itv1, IntegerInterval.Full), IntegerInterval.Unicode)
    }

    // TESTS ------------------------------------------------------------------

    @Test
    fun binarySearch() {
        val itv = complexItv
        Assertions.assertEquals(0, itv.binarySearch(complexRange1.from))
        Assertions.assertEquals(1, itv.binarySearch(complexRange2.to))
        Assertions.assertEquals(2, itv.binarySearch((complexRange3.from + complexRange3.to) / 2))

        Assertions.assertEquals(-1, itv.binarySearch(complexRange1.from - 1))
        Assertions.assertEquals(-2, itv.binarySearch(complexRange1.to + 1))
        Assertions.assertEquals(-2, itv.binarySearch(complexRange2.from - 1))
        Assertions.assertEquals(-3, itv.binarySearch(complexRange2.to + 1))
    }

    @Test
    fun pointCount() {
        var itv = complexItv
        Assertions.assertEquals(
                complexRange1.pointCount + complexRange2.pointCount + complexRange3.pointCount + complexRange4.pointCount + complexRange5.pointCount,
                itv.pointCount)

        itv = IntegerInterval.Empty
        Assertions.assertEquals(0, itv.pointCount)
    }

    @Test
    fun rangeCount() {
        var itv = complexItv
        Assertions.assertEquals(5, itv.rangeCount)

        itv = IntegerInterval.Empty
        Assertions.assertEquals(0, itv.rangeCount)
    }

    @Test
    fun getter() {
        val itv = complexItv
        Assertions.assertEquals(complexRange1.from, itv[0])
        Assertions.assertEquals(complexRange1.to, itv[complexRange1.pointCount.toInt() - 1])
        Assertions.assertEquals(complexRange2.from, itv[complexRange1.pointCount.toInt()])
        Assertions.assertEquals(complexRange5.to,
                itv[complexRange1.pointCount.toInt() + complexRange2.pointCount.toInt() + complexRange3.pointCount.toInt() + complexRange4.pointCount.toInt() + complexRange5.pointCount.toInt() - 1])

        Assertions.assertNull(itv[-1])
        Assertions.assertNull(itv[200000])
    }

    @Test
    fun rangeAtOrNull() {
        val itv = complexItv
        Assertions.assertEquals(complexRange1, itv.rangeAtOrNull(0))
        Assertions.assertEquals(complexRange2, itv.rangeAtOrNull(1))
        Assertions.assertEquals(complexRange3, itv.rangeAtOrNull(2))
        Assertions.assertEquals(complexRange4, itv.rangeAtOrNull(3))
        Assertions.assertEquals(complexRange5, itv.rangeAtOrNull(4))

        Assertions.assertNull(itv.rangeAtOrNull(-1))
        Assertions.assertNull(itv.rangeAtOrNull(200000))
    }

    @Test
    fun contains() {
        var itv = complexItv
        Assertions.assertTrue(itv.contains(complexRange1.from))
        Assertions.assertTrue(itv.contains(complexRange2.to))
        Assertions.assertTrue(itv.contains((complexRange3.from + complexRange3.to) / 2))
        Assertions.assertFalse(itv.contains(complexRange2.from - 1))
        Assertions.assertFalse(itv.contains(complexRange2.to + 1))

        itv = IntegerInterval.Empty
        Assertions.assertFalse(itv.contains(-5))
        Assertions.assertFalse(itv.contains(0))
        Assertions.assertFalse(itv.contains(5))
    }

    @Test
    fun isEmpty() {
        var itv = IntegerInterval.new(complexRange1)
        Assertions.assertFalse(itv.isEmpty)

        itv = IntegerInterval.Empty
        Assertions.assertTrue(itv.isEmpty)

        itv = complexItv
        Assertions.assertFalse(itv.isEmpty)
    }

    @Test
    fun firstPoint() {
        var itv = IntegerInterval.new(complexRange1)
        Assertions.assertEquals(complexRange1.from, itv.firstPoint)

        itv = IntegerInterval.Empty
        Assertions.assertNull(itv.firstPoint)

        itv = complexItv
        Assertions.assertEquals(complexRange1.from, itv.firstPoint)
    }

    @Test
    fun lastPoint() {
        var itv = IntegerInterval.new(complexRange1)
        Assertions.assertEquals(complexRange1.to, itv.lastPoint)

        itv = IntegerInterval.Empty
        Assertions.assertNull(itv.lastPoint)

        itv = complexItv
        Assertions.assertEquals(complexRange5.to, itv.lastPoint)
    }

    @Test
    fun iteratorTest() {
        var itv = IntegerInterval.Empty
        for (range in itv) {
            throw AssertionError("The Empty interval must have no points inside.")
        }

        itv = complexItv
        var point = complexRange1.from
        for (itvPoint in itv) {
            Assertions.assertEquals(point, itvPoint)

            when (point) {
                complexRange1.to -> point = complexRange2.from
                complexRange2.to -> point = complexRange3.from
                complexRange3.to -> point = complexRange4.from
                complexRange4.to -> point = complexRange5.from
                else -> point += 1
            }
        }
    }

    @Test
    fun rangeIteratorTest() {
        var itv = IntegerInterval.Empty
        for (range in itv.rangeIterator()) {
            throw AssertionError("The Empty interval must have no ranges inside.")
        }

        itv = complexItv
        var index = 0
        for (range in itv.rangeIterator()) {
            when (index) {
                0 -> Assertions.assertEquals(complexRange1, range)
                1 -> Assertions.assertEquals(complexRange2, range)
                2 -> Assertions.assertEquals(complexRange3, range)
                3 -> Assertions.assertEquals(complexRange4, range)
                4 -> Assertions.assertEquals(complexRange5, range)
                else -> throw AssertionError("Too much range inside complexItv.")
            }

            index += 1
        }
    }

    @Test
    fun equalsTest() {
        val itv1 = IntegerInterval.new(complexRange1)
        val itv2 = complexItv
        val itv3 = IntegerInterval.Empty + complexRange1 + complexRange2 + complexRange3 + complexRange4 + complexRange5

        Assertions.assertTrue(itv1 == itv1)
        Assertions.assertTrue(itv2 == itv2)
        Assertions.assertTrue(itv3 == itv3)
        Assertions.assertTrue(itv2 == itv3)
        Assertions.assertFalse(itv1 == itv2)
    }

    @Test
    fun hashCodeTest() {
        val itv1 = IntegerInterval.new(complexRange1)
        val itv2 = complexItv
        val itv3 = IntegerInterval.Empty + complexRange1 + complexRange2 + complexRange3 + complexRange4 + complexRange5

        Assertions.assertTrue(itv1.hashCode() == itv1.hashCode())
        Assertions.assertTrue(itv2.hashCode() == itv2.hashCode())
        Assertions.assertTrue(itv3.hashCode() == itv3.hashCode())
        Assertions.assertTrue(itv2.hashCode() == itv3.hashCode())
        Assertions.assertFalse(itv1.hashCode() == itv2.hashCode())
    }

    @Test
    fun toStringTest() {
        var itv = IntegerInterval.Empty
        Assertions.assertEquals("[]", itv.toString())

        itv = IntegerInterval.new(complexRange1)
        Assertions.assertEquals("[${complexRange1.from}, ${complexRange1.to}]", itv.toString())

        itv = IntegerInterval.new(IntegerRange.new(complexRange2.from))
        Assertions.assertEquals("[${complexRange2.from}]", itv.toString())

        Assertions.assertEquals("$complexRange1 U $complexRange2 U $complexRange3 U $complexRange4 U $complexRange5",
                complexItv.toString())
    }

    @Test
    fun toHexString() {
        var itv = IntegerInterval.Empty
        Assertions.assertEquals("[]", itv.toHexString())

        itv = IntegerInterval.new(complexRange1)
        Assertions.assertEquals("[${complexRange1.from.toString(16)}, ${complexRange1.to.toString(16)}]",
                itv.toHexString())

        itv = IntegerInterval.new(IntegerRange.new(complexRange2.from))
        Assertions.assertEquals("[${complexRange2.from.toString(16)}]", itv.toHexString())

        Assertions.assertEquals(
                "${complexRange1.toHexString()} U ${complexRange2.toHexString()} U ${complexRange3.toHexString()} U ${complexRange4.toHexString()} U ${complexRange5.toHexString()}",
                complexItv.toHexString())
    }

    @Test
    fun emptyValue() {
        val itv = IntegerInterval.Empty
        Assertions.assertNotNull(itv)
        Assertions.assertNull(itv.firstPoint)
        Assertions.assertNull(itv.lastPoint)
        Assertions.assertEquals(0, itv.rangeCount)
        Assertions.assertEquals("[]", itv.toString())
    }
}
