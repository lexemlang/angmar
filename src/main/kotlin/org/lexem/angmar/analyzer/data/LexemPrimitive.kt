package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The common part of every primitive in lexem.
 */
internal interface LexemPrimitive : LexemMemoryValue {
    /**
     * Dereferences all indirect references until get a value that is not a [LxmReference].
     */
    fun dereference(memory: LexemMemory): LexemMemoryValue = this

    /**
     * Gets the hash of the current value.
     */
    fun getHashCode(memory: LexemMemory): Int
}
