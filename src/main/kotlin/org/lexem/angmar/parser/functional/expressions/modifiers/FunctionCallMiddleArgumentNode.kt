package org.lexem.angmar.parser.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for middle arguments of function call.
 */
class FunctionCallMiddleArgumentNode private constructor(parser: LexemParser, val identifier: ParserNode,
        val expression: ParserNode) : ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(identifier)
        append(relationalToken)
        append(expression)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("identifier", identifier)
        printer.addField("expression", expression)
    }

    companion object {
        const val relationalToken = GlobalCommons.relationalToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a middle argument of function call.
         */
        fun parse(parser: LexemParser): FunctionCallMiddleArgumentNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionCallMiddleArgumentNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val identifier = Commons.parseDynamicIdentifier(parser) ?: return null

            WhitespaceNode.parse(parser)

            if (!parser.readText(relationalToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            val expression = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.FunctionCallMiddleArgumentWithoutExpressionAfterRelationalToken,
                    "An expression was expected after the relational token '$relationalToken'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an expression here")
                }
            }

            val result = FunctionCallMiddleArgumentNode(parser, identifier, expression)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
