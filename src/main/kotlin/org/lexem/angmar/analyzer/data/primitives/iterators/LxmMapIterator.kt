package org.lexem.angmar.analyzer.data.primitives.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The lexem value of a Map iterator.
 */
internal class LxmMapIterator : LexemIterator {
    val reference: LxmReference
    val value: LxmMap
    private val keys: List<LexemPrimitive>

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, reference: LxmReference) {
        this.reference = reference
        this.value = reference.dereferenceAs(memory)!!
        reference.increaseReferenceCount(memory)

        keys = value.getAllProperties().flatMap { it.value.map { it.key } }
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
        return Pair(currentKey, currentValue)
    }

    override fun finalize(memory: LexemMemory) {
        reference.decreaseReferenceCount(memory)
    }

    override fun toString() = "[Iterator - Map](value: $value, index: $index, isEnded: $isEnded)"
}
