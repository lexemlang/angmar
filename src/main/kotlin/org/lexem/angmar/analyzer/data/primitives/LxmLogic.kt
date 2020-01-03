package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.literals.*

/**
 * The Lexem values of the Logic type.
 */
internal class LxmLogic private constructor(val primitive: Boolean) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, LogicType.TypeName) as LxmReference
    }

    override fun getHashCode() = primitive.hashCode()

    override fun toLexemString(memory: IMemory) = if (primitive) {
        LxmString.True
    } else {
        LxmString.False
    }

    override fun toString() = if (primitive) {
        LogicNode.trueLiteral
    } else {
        LogicNode.falseLiteral
    }

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
