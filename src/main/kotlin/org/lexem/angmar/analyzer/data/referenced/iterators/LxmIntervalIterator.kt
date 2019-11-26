package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.data.*

/**
 * The Lexem value of a Interval iterator.
 */
internal class LxmIntervalIterator : LexemIterator {
    val value: IntegerInterval

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, value: IntegerInterval) : super(memory) {
        this.value = value
        this.size = value.pointCount
    }

    private constructor(oldIterator: LxmIntervalIterator) : super(oldIterator) {
        this.value = oldIterator.value
        this.size = oldIterator.size
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override val size: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val index = getIndex(memory)
        val currentValue = value[index.primitive]!!
        return Pair(null, LxmInteger.from(currentValue))
    }

    override fun clone() = LxmIntervalIterator(this)

    override fun toString() = "[ITERATOR - INTERVAL] (value: $value) - ${super.toString()}"
}
