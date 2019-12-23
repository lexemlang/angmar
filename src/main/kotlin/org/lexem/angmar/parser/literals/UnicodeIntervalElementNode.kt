package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for elements of unicode interval literals.
 */
internal class UnicodeIntervalElementNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
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

    override fun toTree(): JsonObject {
        val result = super.toTree()

        if (left == null) {
            result.addProperty("left", leftChar.toString())
        }
        result.add("left", left?.toTree())


        if (right == null && rightChar != ' ') {
            result.addProperty("right", rightChar.toString())
        }
        result.add("right", right?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            UnicodeIntervalElementCompiled.compile(parent, parentSignal, this)

    companion object {
        const val rangeToken = IntervalElementNode.rangeToken
        private val forbiddenChars =
                listOf(UnicodeIntervalSubIntervalNode.startToken, UnicodeIntervalSubIntervalNode.endToken,
                        UnicodeIntervalAbbrNode.endToken)

        /**
         * Parses an element of unicode interval literals.
         */
        fun parse(parser: LexemParser, parent: ParserNode): UnicodeIntervalElementNode? {
            val initCursor = parser.reader.saveCursor()
            val result = UnicodeIntervalElementNode(parser, parent)

            result.left = Commons.parseAnyEscape(parser, result)
            if (result.left == null) {
                result.leftChar = readIntervalChar(parser) ?: return null
            }

            if (parser.readText(rangeToken)) {
                result.right = Commons.parseAnyEscape(parser, result)
                if (result.right == null) {
                    result.rightChar = readIntervalChar(parser) ?: throw AngmarParserException(
                            AngmarParserExceptionType.UnicodeIntervalElementWithoutElementAfterRangeOperator,
                            "An escape or character was expected after the range operator '$rangeToken'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding an escape or character here"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection((result.left?.to?.position() ?: initCursor.position()) + 1,
                                    parser.reader.currentPosition() - 1)
                            message = "Try removing the '$rangeToken' operator"
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
