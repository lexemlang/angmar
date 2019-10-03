package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The lexem values of the Float type.
 */
internal class LxmFloat private constructor(val primitive: Float) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, FloatType.TypeName)

    override fun getHashCode(memory: LexemMemory) = primitive.hashCode()

    override fun toString() = primitive.toString()

    // STATIC -----------------------------------------------------------------

    companion object {
        val Num_1 = LxmFloat(-1.0f)
        val Num0 = LxmFloat(0.0f)
        val Num1 = LxmFloat(1.0f)

        /**
         * Returns the [LxmFloat] equivalent of the specified float value.
         */
        fun from(value: Float) = when (value) {
            -1.0f -> Num_1
            0.0f -> Num0
            1.0f -> Num1
            else -> LxmFloat(value)
        }
    }
}
