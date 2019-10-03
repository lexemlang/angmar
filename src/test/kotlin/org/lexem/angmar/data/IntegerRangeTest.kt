package org.lexem.angmar.data

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.utils.*

internal class IntegerRangeTest {
    private val from = 40 // greater than 10
    private val to = 70

    @Test
    fun constructor() {
        var range = IntegerRange.new(from)
        Assertions.assertNotNull(range)
        Assertions.assertEquals(from, range.from)
        Assertions.assertEquals(from, range.to)

        range = IntegerRange.new(from, from)
        Assertions.assertNotNull(range)
        Assertions.assertEquals(from, range.from)
        Assertions.assertEquals(from, range.to)

        range = IntegerRange.new(from, to)
        Assertions.assertNotNull(range)
        Assertions.assertEquals(from, range.from)
        Assertions.assertEquals(to, range.to)
    }

    @Test
    @Incorrect
    fun `incorrect constructor`() {
        TestUtils.assertAngmarException {
            IntegerRange.new(to, from)
        }
    }

    @Test
    fun pointCount() {
        Assertions.assertEquals(1, IntegerRange.new(from).pointCount)
        Assertions.assertEquals(1, IntegerRange.new(from, from).pointCount)
        Assertions.assertEquals(to - from + 1L, IntegerRange.new(from, to).pointCount)
    }

    @Test
    fun getter() {
        var range = IntegerRange.new(0, to)
        var point = 0
        var index = 0
        while (index < range.pointCount) {
            Assertions.assertEquals(point, range[index])
            point += 1
            index += 1
        }

        Assertions.assertNull(range[-1])
        Assertions.assertNull(range[range.from - 1])
        Assertions.assertNull(range[range.to + 1])

        range = IntegerRange.new(from, to)
        point = from
        index = 0
        while (index < range.pointCount) {
            Assertions.assertEquals(point, range[index])
            point += 1
            index += 1
        }

        Assertions.assertNull(range[-1])
        Assertions.assertNull(range[range.pointCount.toInt()])
        Assertions.assertNull(range[range.pointCount.toInt() + 1])
    }

    @Test
    fun contains() {
        val range = IntegerRange.new(from, to)
        var point = from
        while (point <= to) {
            Assertions.assertTrue(point in range)
            point += 1
        }

        Assertions.assertTrue(from - 1 !in range)
        Assertions.assertTrue(to + 1 !in range)
    }

    @Test
    fun isNearTo() {
        val range = IntegerRange.new(from, to)
        var rangeAux = IntegerRange.new(from - 10, from - 2)
        Assertions.assertFalse(range.isNearTo(rangeAux))
        Assertions.assertFalse(rangeAux.isNearTo(range))

        rangeAux = IntegerRange.new(from - 10, from - 1)
        Assertions.assertTrue(range.isNearTo(rangeAux))
        Assertions.assertTrue(rangeAux.isNearTo(range))

        rangeAux = IntegerRange.new(from - 10, from)
        Assertions.assertTrue(range.isNearTo(rangeAux))
        Assertions.assertTrue(rangeAux.isNearTo(range))

        rangeAux = IntegerRange.new(from - 10, (from + to) / 2)
        Assertions.assertTrue(range.isNearTo(rangeAux))
        Assertions.assertTrue(rangeAux.isNearTo(range))

        rangeAux = IntegerRange.new(from - 10, to)
        Assertions.assertTrue(range.isNearTo(rangeAux))
        Assertions.assertTrue(rangeAux.isNearTo(range))

        // TO
        rangeAux = IntegerRange.new(to + 2, to + 10)
        Assertions.assertFalse(range.isNearTo(rangeAux))
        Assertions.assertFalse(rangeAux.isNearTo(range))

        rangeAux = IntegerRange.new(to + 1, to + 10)
        Assertions.assertTrue(range.isNearTo(rangeAux))
        Assertions.assertTrue(rangeAux.isNearTo(range))

        rangeAux = IntegerRange.new(to, to + 10)
        Assertions.assertTrue(range.isNearTo(rangeAux))
        Assertions.assertTrue(rangeAux.isNearTo(range))

        rangeAux = IntegerRange.new((from + to) / 2, to + 10)
        Assertions.assertTrue(range.isNearTo(rangeAux))
        Assertions.assertTrue(rangeAux.isNearTo(range))

        rangeAux = IntegerRange.new(from, to + 10)
        Assertions.assertTrue(range.isNearTo(rangeAux))
        Assertions.assertTrue(rangeAux.isNearTo(range))
    }

    @Test
    fun `test compareTo`() {
        val range = IntegerRange.new(from, to)

        Assertions.assertEquals(1, range.compareTo(from - 1))
        Assertions.assertEquals(0, range.compareTo(from))
        Assertions.assertEquals(0, range.compareTo((from + to) / 2))
        Assertions.assertEquals(0, range.compareTo(to))
        Assertions.assertEquals(-1, range.compareTo(to + 1))
    }

    @Test
    fun iterator() {
        var range = IntegerRange.new(0, to)
        var point = 0
        for (itPoint in range) {
            Assertions.assertEquals(point, itPoint)
            point += 1
        }

        range = IntegerRange.new(from, to)
        point = from
        for (itPoint in range) {
            Assertions.assertEquals(point, itPoint)
            point += 1
        }
    }

    @Test
    fun equalsTest() {
        val range1 = IntegerRange.new(0, to)
        val range2 = IntegerRange.new(from, to)
        val range3 = IntegerRange.new(from, to)

        Assertions.assertTrue(range1 == range1)
        Assertions.assertTrue(range2 == range2)
        Assertions.assertTrue(range3 == range3)
        Assertions.assertTrue(range2 == range3)
        Assertions.assertFalse(range1 == range2)
    }

    @Test
    fun hashCodeTest() {
        val range1 = IntegerRange.new(0, to)
        val range2 = IntegerRange.new(from, to)
        val range3 = IntegerRange.new(from, to)

        Assertions.assertTrue(range1.hashCode() == range1.hashCode())
        Assertions.assertTrue(range2.hashCode() == range2.hashCode())
        Assertions.assertTrue(range3.hashCode() == range3.hashCode())
        Assertions.assertTrue(range2.hashCode() == range3.hashCode())
        Assertions.assertFalse(range1.hashCode() == range2.hashCode())
    }

    @Test
    fun toStringTest() {
        var range = IntegerRange.new(from, to)
        Assertions.assertEquals("[$from, $to]", range.toString())

        range = IntegerRange.new(from)
        Assertions.assertEquals("[$from]", range.toString())
    }

    @Test
    fun toHexStringTest() {
        var range = IntegerRange.new(from, to)
        Assertions.assertEquals("[${from.toString(16)}, ${to.toString(16)}]", range.toHexString())

        range = IntegerRange.new(from)
        Assertions.assertEquals("[${from.toString(16)}]", range.toHexString())
    }
}
