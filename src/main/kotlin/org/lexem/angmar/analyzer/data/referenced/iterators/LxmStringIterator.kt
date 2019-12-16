package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*

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

    private constructor(memory: LexemMemory, oldVersion: LxmStringIterator, toClone: Boolean) : super(memory,
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
        val currentValue = value[index.primitive]
        return Pair(null, LxmString.from("$currentValue"))
    }

    override fun clone(memory: LexemMemory) = LxmStringIterator(memory, this,
            toClone = (countOldVersions() ?: 0) >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[ITERATOR - STRING] (value: $value) - ${super.toString()}"
}
