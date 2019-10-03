package org.lexem.angmar.analyzer.data.primitives.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.data.*

/**
 * The lexem value of a Interval iterator.
 */
internal class LxmIntervalIterator(val value: IntegerInterval) : LexemIterator {

    // OVERRIDE METHODS -------------------------------------------------------

    override var index = 0

    override val size = value.pointCount

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded) {
            return null
        }

        val currentValue = value[index]!!
        return Pair(null, LxmInteger.from(currentValue))
    }

    override fun toString() = "[Iterator - Interval](value: $value, index: $index, isEnded: $isEnded)"
}
