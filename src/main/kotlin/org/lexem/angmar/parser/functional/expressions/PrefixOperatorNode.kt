package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*


/**
 * Parser for prefix operators.
 */
internal class PrefixOperatorNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var operator = ""

    override fun toString() = operator

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("operator", operator)

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) = throw AngmarUnreachableException()

    companion object {
        const val bitwiseNegationOperator = "~"
        const val notOperator = "!"
        const val affirmationOperator = "+"
        const val negationOperator = "-"
        val operators = listOf(bitwiseNegationOperator, notOperator, affirmationOperator, negationOperator)

        // METHODS ------------------------------------------------------------

        /**
         * Parses a prefix operator
         */
        fun parse(parser: LexemParser, parent: ParserNode): PrefixOperatorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PrefixOperatorNode(parser, parent)

            result.operator = ExpressionsCommons.readPrefixOperator(parser, operators.asSequence()) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
