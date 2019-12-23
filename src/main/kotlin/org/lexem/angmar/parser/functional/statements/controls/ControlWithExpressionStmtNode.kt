package org.lexem.angmar.parser.functional.statements.controls

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.controls.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for control statements with a expression.
 */
internal class ControlWithExpressionStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var keyword: String
    lateinit var expression: ParserNode
    var tag: IdentifierNode? = null

    override fun toString() = StringBuilder().apply {
        append(keyword)
        if (tag != null) {
            append(GlobalCommons.tagPrefix)
            append(tag)
        }
        append(' ')
        append(expression)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("keyword", keyword)
        result.add("tag", tag?.toTree())
        result.add("expression", expression.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            ControlWithExpressionStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        const val returnKeyword = "return"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a control statement with a expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, keyword: String,
                captureTag: Boolean = true): ControlWithExpressionStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ControlWithExpressionStmtNode(parser, parent)
            result.keyword = keyword

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            if (captureTag) {
                let {
                    val initTagCursor = parser.reader.saveCursor()

                    if (!parser.readText(GlobalCommons.tagPrefix)) {
                        return@let
                    }

                    result.tag = IdentifierNode.parse(parser, result)
                    if (result.tag == null) {
                        initTagCursor.restore()
                    }
                }
            }

            WhitespaceNode.parse(parser)

            result.expression = ExpressionsCommons.parseExpression(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ControlWithExpressionStatementWithoutExpression,
                    "An expression was expected after the control statement '$keyword'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message = "Try adding a whitespace followed by an expression here e.g. '${NilNode.nilLiteral}'"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
