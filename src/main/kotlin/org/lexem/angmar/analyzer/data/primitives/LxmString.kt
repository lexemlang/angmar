package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The Lexem values of the String type.
 */
internal class LxmString private constructor(val primitive: String) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, StringType.TypeName)

    override fun getHashCode(memory: LexemMemory) = primitive.hashCode()

    override fun toString() = primitive

    // STATIC -----------------------------------------------------------------

    companion object {
        val Empty = LxmString("")
        val Nil = LxmString("nil")
        val True = LxmString("true")
        val False = LxmString("false")

        /**
         * Returns the [LxmString] equivalent of a string value.
         */
        fun from(value: String) = when (value) {
            Empty.primitive -> Empty
            Nil.primitive -> Nil
            True.primitive -> True
            False.primitive -> False
            else -> LxmString(value)
        }
    }
}
