package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value of a String iterator.
 */
internal class LxmStringIterator : LexemIterator {
    val value: String

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, value: String) : super(memory) {
        this.value = value
        intervalSize = value.length.toLong()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmStringIterator) : super(memory, oldVersion) {
        value = oldVersion.value
        intervalSize = oldVersion.intervalSize
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override val intervalSize: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val index = getIndex(memory)
        val currentValue = value[index.primitive]
        return Pair(null, LxmString.from("$currentValue"))
    }

    override fun memoryShift(memory: LexemMemory) = LxmStringIterator(memory, oldVersion = this)

    override fun toString() = "[Iterator - String] (value: $value) - ${super.toString()}"
}
