package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.errors.*

/**
 * The Lexem values of a control statement.
 */
internal class LxmControl private constructor(val type: String, val tag: String?, val value: LexemPrimitive?,
        val node: CompiledNode) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun increaseReferences(memory: IMemory) {
        value?.increaseReferences(memory)
    }

    override fun decreaseReferences(memory: IMemory) {
        value?.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(memory: IMemory, gcFifo: GarbageCollectorFifo) {
        value?.spatialGarbageCollect(memory, gcFifo)
    }

    override fun getHashCode() = throw AngmarUnreachableException()

    override fun toString() = StringBuilder().apply {
        append("[Control] ")
        append(type)

        if (tag != null) {
            append("@$tag")
        }

        if (value != null) {
            append(" $value")
        }
    }.toString()

    // STATIC -----------------------------------------------------------------

    companion object {
        /**
         * Returns a new [LxmControl].
         */
        fun from(type: String, tag: String?, value: LexemPrimitive?, node: CompiledNode) =
                LxmControl(type, tag, value, node)
    }
}
