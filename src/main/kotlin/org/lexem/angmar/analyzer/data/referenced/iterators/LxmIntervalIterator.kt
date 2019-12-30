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
        intervalSize = value.pointCount
    }

    private constructor(bigNode: BigNode, oldVersion: LxmIntervalIterator) : super(bigNode, oldVersion) {
        value = oldVersion.value
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded()) {
            return null
        }

        val index = getIndex()
        val currentValue = value[index.primitive]!!
        return Pair(null, LxmInteger.from(currentValue))
    }

    override fun memoryClone(bigNode: BigNode) = LxmIntervalIterator(bigNode, this)

    override fun toString() = "[Iterator - Interval] (value: $value) - ${super.toString()}"
}
