package org.lexem.angmar.parser.functional.statements.controls

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for control statements with a expression.
 */
class ControlWithExpressionStmtNode private constructor(parser: LexemParser, val keyword: String,
        val expression: ParserNode) : ParserNode(parser) {
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

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("keyword", keyword)
        printer.addOptionalField("tag", tag)
        printer.addField("expression", expression)
    }

    companion object {
        const val exitKeyword = "exit"
        const val returnKeyword = "return"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a control statement with a expression.
         */
        fun parse(parser: LexemParser, keyword: String, captureTag: Boolean = true): ControlWithExpressionStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ControlWithExpressionStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            var tag: IdentifierNode? = null
            if (captureTag) {
                let {
                    val initTagCursor = parser.reader.saveCursor()

                    if (!parser.readText(GlobalCommons.tagPrefix)) {
                        return@let
                    }

                    tag = IdentifierNode.parse(parser)
                    if (tag == null) {
                        initTagCursor.restore()
                    }
                }
            }

            WhitespaceNode.parse(parser)

            val expression = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ControlWithExpressionStatementWithoutExpression,
                    "An expression was expected after the control statement '$keyword'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding a whitespace followed by an expression here e.g. '${NilNode.nilLiteral}'")
                }
            }

            val result = ControlWithExpressionStmtNode(parser, keyword, expression)
            result.tag = tag
            return parser.finalizeNode(result, initCursor)
        }
    }
}
