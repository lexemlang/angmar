package org.lexem.angmar.analyzer.data.primitives.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The lexem value of a List iterator.
 */
internal class LxmSetIterator : LexemIterator {
    val reference: LxmReference
    val value: List<LexemPrimitive>

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, reference: LxmReference) {
        this.reference = reference
        this.value = reference.dereferenceAs<LxmSet>(memory)!!.getAllValues().flatMap { it.value.map { it.value } }

        size = value.size.toLong()
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override var index = 0

    override val size: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded) {
            return null
        }

        val currentValue = value.getOrNull(index) ?: LxmNil
        return Pair(null, currentValue)
    }

    override fun finalize(memory: LexemMemory) {
        reference.decreaseReferenceCount(memory)
    }

    override fun toString() = "[Iterator - Set](value: $value, index: $index, isEnded: $isEnded)"
}
