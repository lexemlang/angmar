package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for elements of interval literals.
 */
class IntervalElementNode private constructor(parser: LexemParser, val left: ParserNode) : ParserNode(parser) {
    var right: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(left)
        if (right != null) {
            append(rangeToken)
            append(right)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("left", left)
        printer.addOptionalField("right", right)
    }

    companion object {
        const val rangeToken = ".."


        // METHODS ------------------------------------------------------------

        /**
         * Parses an element of interval literals.
         */
        fun parse(parser: LexemParser): IntervalElementNode? {
            parser.fromBuffer(parser.reader.currentPosition(), IntervalElementNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val left = EscapedExpressionNode.parse(parser) ?: NumberNode.parseAnyIntegerDefaultDecimal(parser)
            ?: return null

            val result = IntervalElementNode(parser, left)

            if (parser.readText(rangeToken)) {
                result.right = EscapedExpressionNode.parse(parser) ?: NumberNode.parseAnyIntegerDefaultDecimal(parser)

                if (result.right == null) {
                    throw AngmarParserException(
                            AngmarParserExceptionType.IntervalElementWithoutElementAfterRangeOperator,
                            "An escape or integer number was expected after the range operator '$rangeToken'.") {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightCursorAt(parser.reader.currentPosition())
                            message("Try adding an escape or integer number here")
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightSection(left.to.position(), parser.reader.currentPosition() - 1)
                            message("Try removing the '$rangeToken' operator")
                        }
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
