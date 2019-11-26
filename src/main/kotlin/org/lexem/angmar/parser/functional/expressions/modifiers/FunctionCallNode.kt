package org.lexem.angmar.parser.functional.expressions.modifiers

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.FunctionCallAnalyzer.signalEndPropertiesExpression
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for function call i.e. element(expression).
 */
internal class FunctionCallNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    val positionalArguments = mutableListOf<ParserNode>()
    val namedArguments = mutableListOf<FunctionCallNamedArgumentNode>()
    val spreadArguments = mutableListOf<ParserNode>()
    var propertiesExpression: FunctionCallExpressionPropertiesNode? = null

    override fun toString() = StringBuilder().apply {
        append(startToken)
        append((positionalArguments + namedArguments + spreadArguments.map { "$spreadOperator$it" }).joinToString(
                "$argumentSeparator "))

        append(endToken)

        if (propertiesExpression != null) {
            append(propertiesExpression)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("positionalArguments", SerializationUtils.listToTest(positionalArguments))
        result.add("namedArguments", SerializationUtils.listToTest(namedArguments))
        result.add("spreadArguments", SerializationUtils.listToTest(spreadArguments))
        result.add("propertiesExpression", propertiesExpression?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionCallAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "("
        const val argumentSeparator = GlobalCommons.elementSeparator
        const val endToken = ")"
        const val spreadOperator = GlobalCommons.spreadOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a function expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): FunctionCallNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionCallNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = FunctionCallNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                return null
            }

            var prev = false

            // Positional arguments
            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (prev) {
                    if (!parser.readText(argumentSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)
                }

                val argument = ExpressionsCommons.parseExpression(parser, result,
                        result.positionalArguments.size + FunctionCallAnalyzer.signalEndFirstArgument)
                if (argument == null) {
                    initLoopCursor.restore()
                    break
                }

                // Check the end
                val initEndCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!parser.checkText(argumentSeparator) && !parser.checkText(endToken)) {
                    initLoopCursor.restore()
                    break
                }

                initEndCursor.restore()

                result.positionalArguments.add(argument)
                prev = true
            }

            // Named arguments
            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (prev) {
                    if (!parser.readText(argumentSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)
                }

                val argument = FunctionCallNamedArgumentNode.parse(parser, result,
                        result.positionalArguments.size + result.namedArguments.size + FunctionCallAnalyzer.signalEndFirstArgument)
                if (argument == null) {
                    initLoopCursor.restore()
                    break
                }

                result.namedArguments.add(argument)
                prev = true
            }

            // Spread arguments
            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (prev) {
                    if (!parser.readText(argumentSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)
                }

                if (!parser.readText(spreadOperator)) {
                    initLoopCursor.restore()
                    break
                }

                val argument = ExpressionsCommons.parseExpression(parser, result,
                        result.positionalArguments.size + result.namedArguments.size + result.spreadArguments.size + FunctionCallAnalyzer.signalEndFirstArgument)
                        ?: throw AngmarParserException(
                                AngmarParserExceptionType.FunctionCallWithoutExpressionAfterSpreadOperator,
                                "An expression was expected after the spread operator '$spreadOperator'.") {
                            val fullText = parser.reader.readAllText()
                            addSourceCode(fullText, parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightCursorAt(parser.reader.currentPosition())
                                message = "Try adding an expression here"
                            }
                        }

                result.spreadArguments.add(argument)
                prev = true
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
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close parenthesis '$endToken' here"
                    }
                }
            }

            result.propertiesExpression =
                    FunctionCallExpressionPropertiesNode.parse(parser, result, signalEndPropertiesExpression)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
