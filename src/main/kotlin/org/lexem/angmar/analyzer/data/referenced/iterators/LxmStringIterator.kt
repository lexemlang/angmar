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
        this.size = value.length.toLong()
    }

    private constructor(oldIterator: LxmStringIterator) : super(oldIterator) {
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
        val currentValue = value[index.primitive]
        return Pair(null, LxmString.from("$currentValue"))
    }

    override fun clone() = LxmStringIterator(this)

    override fun toString() = "[ITERATOR - STRING] (value: $value) - ${super.toString()}"
}
