package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * The common part of every primitive in lexem.
 */
internal interface LexemPrimitive : LexemMemoryValue {
    /**
     * Gets the hash of the current value.
     */
    fun getHashCode(): Int

    /**
     * Gets the reference of this value.
     */
    override fun getPrimitive() = this

    override fun getType(memory: IMemory): LxmReference = throw AngmarUnreachableException()
}
