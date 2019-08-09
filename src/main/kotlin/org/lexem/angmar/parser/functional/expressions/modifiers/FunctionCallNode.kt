package org.lexem.angmar.parser.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for function call i.e. element(expression).
 */
class FunctionCallNode private constructor(parser: LexemParser) : ParserNode(parser) {
    val arguments = mutableListOf<ParserNode>()
    var spreadArgument: ParserNode? = null
    var expressionProperties: FunctionCallExpressionPropertiesNode? = null

    override fun toString() = StringBuilder().apply {
        append(startToken)
        append(arguments.joinToString("$argumentSeparator "))

        if (spreadArgument != null) {
            if (arguments.isNotEmpty()) {
                append("$argumentSeparator ")
            }

            append(spreadOperator)
            append(spreadArgument)
        }

        append(endToken)

        if (expressionProperties != null) {
            append(expressionProperties)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("argumentList", arguments)
        printer.addOptionalField("spreadArgument", spreadArgument)
        printer.addOptionalField("expressionProperties", expressionProperties)
    }

    companion object {
        const val startToken = "("
        const val argumentSeparator = GlobalCommons.elementSeparator
        const val endToken = ")"
        const val spreadOperator = GlobalCommons.spreadOperator


        // METHODS ------------------------------------------------------------

        /**
         * Parses a function expression.
         */
        fun parse(parser: LexemParser): FunctionCallNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionCallNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = FunctionCallNode(parser)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            var argument = FunctionCallMiddleArgumentNode.parse(parser) ?: ExpressionsCommons.parseExpression(parser)
            if (argument != null) {
                result.arguments.add(argument)

                while (true) {
                    val initLoopCursor = parser.reader.saveCursor()

                    WhitespaceNode.parse(parser)

                    if (!parser.readText(argumentSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)

                    argument = FunctionCallMiddleArgumentNode.parse(parser)
                    if (argument == null) {
                        initLoopCursor.restore()
                        break
                    }

                    result.arguments.add(argument)
                }
            }

            // Spread element.
            let {
                val initSpreadCursor = parser.reader.saveCursor()

                if (result.arguments.isNotEmpty()) {

                    WhitespaceNode.parse(parser)

                    if (!parser.readText(argumentSeparator)) {
                        initSpreadCursor.restore()
                        return@let
                    }

                    WhitespaceNode.parse(parser)
                }

                if (!parser.readText(spreadOperator)) {
                    initSpreadCursor.restore()
                    return@let
                }

                result.spreadArgument = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.FunctionCallWithoutExpressionAfterSpreadOperator,
                        "An expression was expected after the spread operator '$spreadOperator'.") {
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

            // Trailing comma.
            let {
                val initTrailingCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!parser.readText(argumentSeparator)) {
                    initTrailingCursor.restore()
                    return@let
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.FunctionCallWithoutEndToken,
                        "The close parenthesis was expected '$endToken'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the close parenthesis '$endToken' here")
                    }
                }
            }

            result.expressionProperties = FunctionCallExpressionPropertiesNode.parse(parser)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
