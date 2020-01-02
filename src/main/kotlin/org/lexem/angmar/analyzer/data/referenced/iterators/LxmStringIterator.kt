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

    constructor(memory: IMemory, value: String) : super(memory) {
        this.value = value
        intervalSize = value.length.toLong()
    }

    private constructor(memory: IMemory, oldVersion: LxmStringIterator) : super(memory, oldVersion) {
        this.value = oldVersion.value
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(memory: IMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val index = getIndex(memory)
        val currentValue = value[index.primitive]
        return Pair(null, LxmString.from("$currentValue"))
    }

    override fun memoryClone(memory: IMemory) = LxmStringIterator(memory, this)

    override fun toString() = "[Iterator - String] (value: $value) - ${super.toString()}"
}
