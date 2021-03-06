package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The Lexem values of the Integer type.
 */
internal class LxmInteger private constructor(val primitive: Int) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, IntegerType.TypeName)

    override fun getHashCode(memory: LexemMemory) = primitive.hashCode()

    override fun toString() = primitive.toString()

    // STATIC -----------------------------------------------------------------

    companion object {
        val Num_1 = LxmInteger(-1)
        val Num0 = LxmInteger(0)
        val Num1 = LxmInteger(1)
        val Num2 = LxmInteger(2)
        val Num10 = LxmInteger(10)

        /**
         * Returns the [LxmInteger] equivalent of the specified integer value.
         */
        fun from(value: Int) = when (value) {
            -1 -> Num_1
            0 -> Num0
            1 -> Num1
            2 -> Num2
            10 -> Num10
            else -> LxmInteger(value)
        }
    }
}
