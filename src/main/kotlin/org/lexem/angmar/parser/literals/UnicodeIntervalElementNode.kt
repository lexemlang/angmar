package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for elements of unicode interval literals.
 */
class UnicodeIntervalElementNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var left: ParserNode? = null
    var right: ParserNode? = null
    var leftChar = ' ' // The whitespace ' ' is forbidden here so it is used as control char
    var rightChar = ' ' // The whitespace ' ' is forbidden here so it is used as control char

    override fun toString() = StringBuilder().apply {
        if (left == null) {
            append(leftChar)
        } else {
            append(left)
        }

        if (right != null || rightChar != ' ') {
            append(rangeToken)

            if (right == null) {
                append(rightChar)
            } else {
                append(right)
            }
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        if (left == null) {
            printer.addField("left", leftChar)
        }
        printer.addOptionalField("left", left)


        if (right == null && rightChar != ' ') {
            printer.addField("right", rightChar)
        }
        printer.addOptionalField("right", right)
    }

    companion object {
        const val rangeToken = IntervalElementNode.rangeToken
        private val forbiddenChars =
                listOf(UnicodeIntervalSubIntervalNode.startToken, UnicodeIntervalSubIntervalNode.endToken,
                        UnicodeIntervalAbbrNode.endToken)

        /**
         * Parses an element of unicode interval literals.
         */
        fun parse(parser: LexemParser): UnicodeIntervalElementNode? {
            parser.fromBuffer(parser.reader.currentPosition(), UnicodeIntervalElementNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = UnicodeIntervalElementNode(parser)

            result.left = Commons.parseAnyEscape(parser)
            if (result.left == null) {
                result.leftChar = readIntervalChar(parser) ?: return null
            }

            if (parser.readText(rangeToken)) {
                result.right = Commons.parseAnyEscape(parser)
                if (result.right == null) {
                    result.rightChar = readIntervalChar(parser) ?: throw AngmarParserException(
                            AngmarParserExceptionType.UnicodeIntervalElementWithoutElementAfterRangeOperator,
                            "An escape or character was expected after the range operator '$rangeToken'.") {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightCursorAt(parser.reader.currentPosition())
                            message("Try adding an escape or character here")
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightSection((result.left?.to?.position() ?: initCursor.position()) + 1,
                                    parser.reader.currentPosition() - 1)
                            message("Try removing the '$rangeToken' operator")
                        }
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }

        private fun readIntervalChar(parser: LexemParser): Char? {
            val ch = parser.reader.currentChar() ?: return null

            if (ch in WhitespaceNode.whitespaceChars || ch in WhitespaceNode.endOfLineChars || "$ch" in forbiddenChars) {
                return null
            }

            parser.reader.advance()
            return ch
        }
    }
}
