package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
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

    private constructor(memory: LexemMemory, oldVersion: LxmIntervalIterator, toClone: Boolean) : super(memory,
            oldVersion, toClone) {
        this.value = oldVersion.value
        this.size = oldVersion.size
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

    override fun memoryShift(memory: LexemMemory) = LxmIntervalIterator(memory, this,
            toClone = countOldVersions() >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[Iterator - Interval] (value: $value) - ${super.toString()}"
}
