package org.lexem.angmar.analyzer.data.primitives.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The lexem value of a List iterator.
 */
internal class LxmListIterator : LexemIterator {
    val reference: LxmReference
    val value: LxmList

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, reference: LxmReference) {
        this.reference = reference
        this.value = reference.dereferenceAs(memory)!!
        reference.increaseReferenceCount(memory)

        size = value.listSize.toLong()
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override var index = 0

    override val size: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded) {
            return null
        }

        val currentValue = value.getCell(memory, index) ?: LxmNil
        return Pair(null, currentValue)
    }

    override fun finalize(memory: LexemMemory) {
        reference.decreaseReferenceCount(memory)
    }

    override fun toString() = "[Iterator - List](value: $value, index: $index, isEnded: $isEnded)"
}
