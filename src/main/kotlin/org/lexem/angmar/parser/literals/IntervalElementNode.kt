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
 * Parser for elements of interval literals.
 */
internal class IntervalElementNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var left: ParserNode
    var right: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(left)
        if (right != null) {
            append(rangeToken)
            append(right)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("left", left.toTree())
        result.add("right", right?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            IntervalElementCompiled.compile(parent, parentSignal, this)

    companion object {
        const val rangeToken = ".."


        // METHODS ------------------------------------------------------------

        /**
         * Parses an element of interval literals.
         */
        fun parse(parser: LexemParser, parent: ParserNode): IntervalElementNode? {
            val initCursor = parser.reader.saveCursor()
            val result = IntervalElementNode(parser, parent)

            result.left =
                    EscapedExpressionNode.parse(parser, result) ?: NumberNode.parseAnyIntegerDefaultDecimal(parser,
                            result) ?: return null

            if (parser.readText(rangeToken)) {
                result.right =
                        EscapedExpressionNode.parse(parser, result) ?: NumberNode.parseAnyIntegerDefaultDecimal(parser,
                                result)

                if (result.right == null) {
                    throw AngmarParserException(
                            AngmarParserExceptionType.IntervalElementWithoutElementAfterRangeOperator,
                            "An escape or integer number was expected after the range operator '$rangeToken'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding an escape or integer number here"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(result.left.to.position(), parser.reader.currentPosition() - 1)
                            message = "Try removing the '$rangeToken' operator"
                        }
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
