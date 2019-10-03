package org.lexem.angmar.analyzer.data.primitives.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The lexem value of a String iterator.
 */
internal class LxmStringIterator(val value: String) : LexemIterator {

    // OVERRIDE METHODS -------------------------------------------------------

    override var index = 0

    override val size = value.length.toLong()

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded) {
            return null
        }

        val currentValue = value[index]
        return Pair(null, LxmString.from("$currentValue"))
    }

    override fun toString() = "[Iterator - String](value: $value, index: $index, isEnded: $isEnded)"
}
