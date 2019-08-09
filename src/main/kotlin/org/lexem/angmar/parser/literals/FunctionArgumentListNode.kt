package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for function argument list.
 */
class FunctionArgumentListNode private constructor(parser: LexemParser) : ParserNode(parser) {
    val arguments = mutableListOf<ParserNode>()
    var spread: IdentifierNode? = null

    override fun toString() = StringBuilder().apply {
        append(startToken)
        append(arguments.joinToString("$argumentSeparator "))

        if (spread != null) {
            if (arguments.isNotEmpty()) {
                append("$argumentSeparator ")
            }
            append(spreadOperator)
            append(spread)
        }
        append(endToken)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("argumentList", arguments)
        printer.addOptionalField("spread", spread)
    }

    companion object {
        const val startToken = "("
        const val argumentSeparator = GlobalCommons.elementSeparator
        const val spreadOperator = GlobalCommons.spreadOperator
        const val endToken = ")"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a function argument list.
         */
        fun parse(parser: LexemParser): FunctionArgumentListNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionArgumentListNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = FunctionArgumentListNode(parser)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            var argument = FunctionArgumentNode.parse(parser) ?: ExpressionsCommons.parseExpression(parser)
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

                    argument = FunctionArgumentNode.parse(parser)
                    if (argument == null) {
                        initLoopCursor.restore()
                        break
                    }

                    result.arguments.add(argument)
                }
            }

            // Spread.
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

                result.spread = IdentifierNode.parse(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.FunctionArgumentListWithoutIdentifierAfterSpreadOperator,
                        "An identifier was expected after the spread operator '$spreadOperator'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding an identifier here")
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
                throw AngmarParserException(AngmarParserExceptionType.FunctionArgumentListWithoutEndToken,
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

            return parser.finalizeNode(result, initCursor)
        }
    }
}
