package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*

/**
 * The lexem values of a control statement.
 */
internal class LxmControl private constructor(val type: String, val tag: String?, val value: LexemPrimitive?,
        val node: ParserNode) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, LogicType.TypeName)

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() = StringBuilder().apply {
        append("[Control]")
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
        fun from(type: String, tag: String?, value: LexemPrimitive?, node: ParserNode) =
                LxmControl(type, tag, value, node)
    }
}
