package org.lexem.angmar.analyzer.data.primitives.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The lexem value of an Object iterator.
 */
internal class LxmObjectIterator : LexemIterator {
    val reference: LxmReference
    val value: LxmObject
    private val keys: List<String>

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, reference: LxmReference) {
        this.reference = reference
        this.value = reference.dereferenceAs(memory)!!
        reference.increaseReferenceCount(memory)

        keys = value.getAllIterableProperties().map { it.key }
        size = keys.size.toLong()
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override var index = 0

    override val size: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded) {
            return null
        }

        val currentKey = keys[index]
        val currentValue = value.getPropertyValue(memory, currentKey) ?: LxmNil
        return Pair(LxmString.from(currentKey), currentValue)
    }

    override fun finalize(memory: LexemMemory) {
        reference.decreaseReferenceCount(memory)
    }

    override fun toString() = "[Iterator - Object](value: $value, index: $index, isEnded: $isEnded)"
}
