package org.lexem.angmar.parser.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*


/**
 * Parser for prefix operators.
 */
class PrefixOperatorNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var operator = ""

    override fun toString() = operator

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("operator", operator)
    }

    companion object {
        const val bitNegationOperator = "~"
        const val notOperator = "!"
        const val affirmationOperator = "+"
        const val negationOperator = "-"
        val operators = listOf(bitNegationOperator, notOperator, affirmationOperator, negationOperator)

        // METHODS ------------------------------------------------------------

        /**
         * Parses a prefix operator
         */
        fun parse(parser: LexemParser): PrefixOperatorNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PrefixOperatorNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = PrefixOperatorNode(parser)

            result.operator = ExpressionsCommons.readPrefixOperator(parser, operators.asSequence()) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
