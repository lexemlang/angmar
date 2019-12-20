package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.parser.*


/**
 * Parser for prefix operators.
 */
internal class PrefixOperatorNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var operator = ""

    override fun toString() = operator

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("operator", operator)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PrefixOperatorAnalyzer.stateMachine(analyzer, signal, this)

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
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): PrefixOperatorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PrefixOperatorNode(parser, parent, parentSignal)

            result.operator = ExpressionsCommons.readPrefixOperator(parser, operators.asSequence()) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
