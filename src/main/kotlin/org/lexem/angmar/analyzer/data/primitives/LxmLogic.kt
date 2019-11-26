package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The Lexem values of the Logic type.
 */
internal class LxmLogic private constructor(val primitive: Boolean) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, LogicType.TypeName)

    override fun getHashCode(memory: LexemMemory) = primitive.hashCode()

    override fun toString() = primitive.toString()

    // STATIC -----------------------------------------------------------------

    companion object {
        val True = LxmLogic(true)
        val False = LxmLogic(false)

        /**
         * Returns the [LxmLogic] equivalent of a boolean value.
         */
        fun from(value: Boolean) = if (value) {
            True
        } else {
            False
        }
    }
}
