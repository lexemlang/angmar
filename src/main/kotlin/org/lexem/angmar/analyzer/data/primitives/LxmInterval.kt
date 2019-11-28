package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.data.*

/**
 * The Lexem values of the Interval type.
 */
internal class LxmInterval private constructor(val primitive: IntegerInterval) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, IntervalType.TypeName) as LxmReference
    }

    override fun getHashCode(memory: LexemMemory) = primitive.hashCode()

    override fun toString() = primitive.toString()

    // STATIC -----------------------------------------------------------------

    companion object {
        val Empty = LxmInterval(IntegerInterval.Empty)
        val Full = LxmInterval(IntegerInterval.Full)

        /**
         * Returns the [LxmInterval] equivalent of an [IntegerInterval] value.
         */
        fun from(value: IntegerInterval) = when (value) {
            Empty.primitive -> Empty
            Full.primitive -> Full
            else -> LxmInterval(value)
        }
    }
}
