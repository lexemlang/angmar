package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*

/**
 * Utils for memory.
 */
internal object MemoryUtils {
    /**
     * Replaces a primitive with a new one handling the pointer changes.
     */
    fun replacePrimitives(bigNode: BigNode, oldValue: LexemPrimitive, newValue: LexemPrimitive) {
        // Increases the new reference.
        if (newValue is LxmReference) {
            bigNode.getHeapCell(newValue.position, toWrite = true).increaseReferences()
        }

        // Removes the previous reference.
        if (oldValue is LxmReference) {
            bigNode.getHeapCell(oldValue.position, toWrite = true).decreaseReferences()
        }
    }
}
