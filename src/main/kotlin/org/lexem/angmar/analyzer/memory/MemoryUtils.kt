package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.*

/**
 * Utils for memory.
 */
internal object MemoryUtils {
    /**
     * Replaces a primitive with a new one handling the pointer changes.
     */
    fun replacePrimitives(memory: IMemory, oldValue: LexemPrimitive, newValue: LexemPrimitive) {
        // Increases the new reference.
        newValue.increaseReferences(memory)

        // Removes the previous reference.
        oldValue.decreaseReferences(memory)
    }
}
