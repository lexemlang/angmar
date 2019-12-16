package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.data.*
import org.lexem.angmar.parser.literals.*

/**
 * The Lexem values of the Interval type.
 */
internal class LxmInterval private constructor(val primitive: IntegerInterval) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, IntervalType.TypeName) as LxmReference
    }

    override fun getHashCode(memory: LexemMemory) = primitive.hashCode()

    override fun toLexemString(memory: LexemMemory) = if (primitive.isEmpty) {
        LxmString.from("[]")
    } else {
        val res = StringBuilder().apply {
            append(IntervalNode.macroName)
            append(IntervalNode.startToken)

            val content = primitive.rangeIterator().asSequence().joinToString(" ") { range ->
                var res = "${NumberNode.hexadecimalPrefix}${range.from.toString(16)}"

                if (range.pointCount != 1L) {
                    res += "${IntervalElementNode.rangeToken}${NumberNode.hexadecimalPrefix}${range.to.toString(16)}"
                }

                res
            }

            append(content)
            append(IntervalNode.endToken)
        }
        LxmString.from(res.toString())
    }


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
