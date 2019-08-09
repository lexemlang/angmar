package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for an element of the bitlist literals.
 */
class BitlistElementNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var number: String? = null
    var expression: EscapedExpressionNode? = null

    override fun toString() = if (number != null) {
        number!!
    } else {
        expression.toString()
    }


    override fun toTree(printer: TreeLikePrinter) {
        printer.addOptionalField("number", number)
        printer.addOptionalField("expression", expression)
    }

    companion object {
        /**
         * Parses an element of the bitlist literal.
         */
        fun parse(parser: LexemParser, radix: Int): BitlistElementNode? {
            parser.fromBuffer(parser.reader.currentPosition(), BitlistElementNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = BitlistElementNode(parser)

            result.expression = EscapedExpressionNode.parse(parser)
            if (result.expression == null) {
                result.number = when (radix) {
                    2 -> NumberNode.readBinaryInteger(parser)
                    8 -> NumberNode.readOctalInteger(parser)
                    16 -> NumberNode.readHexadecimalInteger(parser)
                    else -> throw AngmarUnimplementedException()
                }

                if (result.number == null) {
                    return null
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
