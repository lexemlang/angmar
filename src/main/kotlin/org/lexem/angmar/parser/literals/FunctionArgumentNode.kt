package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for function arguments.
 */
class FunctionArgumentNode private constructor(parser: LexemParser, val identifier: ParserNode) : ParserNode(parser) {
    var expression: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(identifier)
        if (expression != null) {
            append(assignOperator)
            append(expression)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("identifier", identifier)
        printer.addOptionalField("expression", expression)
    }

    companion object {
        const val assignOperator = AssignOperatorNode.assignOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a function argument.
         */
        fun parse(parser: LexemParser): FunctionArgumentNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionArgumentNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val identifier = Commons.parseDynamicIdentifier(parser) ?: return null

            val result = FunctionArgumentNode(parser, identifier)

            // Assign
            let {
                val initAssignCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!parser.readText(assignOperator)) {
                    initAssignCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.expression = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.FunctionArgumentWithoutExpressionAfterAssignOperator,
                        "An expression was expected after the assign operator '$assignOperator' to act as the default value of the function argument.") {
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
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
