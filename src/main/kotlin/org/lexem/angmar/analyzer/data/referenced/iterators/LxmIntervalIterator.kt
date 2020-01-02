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

    constructor(memory: IMemory, value: IntegerInterval) : super(memory) {
        this.value = value
        intervalSize = value.pointCount
    }

    private constructor(memory: IMemory, oldVersion: LxmIntervalIterator) : super(memory, oldVersion) {
        value = oldVersion.value
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(memory: IMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val index = getIndex(memory)
        val currentValue = value[index.primitive]!!
        return Pair(null, LxmInteger.from(currentValue))
    }

    override fun memoryClone(memory: IMemory) = LxmIntervalIterator(memory, this)

    override fun toString() = "[Iterator - Interval] (value: $value) - ${super.toString()}"
}
