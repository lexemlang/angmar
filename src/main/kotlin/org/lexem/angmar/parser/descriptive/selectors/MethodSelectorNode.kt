package org.lexem.angmar.parser.descriptive.selectors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for methods of selectors.
 */
internal class MethodSelectorNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    var argument: ParserNode? = null
    lateinit var name: IdentifierNode

    override fun toString() = StringBuilder().apply {
        append(relationalToken)

        if (isNegated) {
            append(notOperator)
        }

        append(name)

        if (argument != null) {
            append(argument)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("name", name.toTree())
        result.add("arguments", argument?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            MethodSelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val relationalToken = GlobalCommons.relationalToken
        const val selectorStartToken = ParenthesisExpressionNode.startToken
        const val selectorEndToken = ParenthesisExpressionNode.endToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a method of a selector.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): MethodSelectorNode? {
            parser.fromBuffer(parser.reader.currentPosition(), MethodSelectorNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = MethodSelectorNode(parser, parent, parentSignal)

            if (!parser.readText(relationalToken)) {
                initCursor.restore()
                return null
            }

            result.isNegated = parser.readText(notOperator)

            result.name = IdentifierNode.parse(parser, result, MethodSelectorAnalyzer.signalEndName)
                    ?: throw AngmarParserException(AngmarParserExceptionType.SelectorMethodWithoutName,
                            "Selector methods require a name to to identify the action to execute.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding an identifier here"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            message = "Try removing the start token '$relationalToken'"
                        }
                    }

            var argument: ParserNode? =
                    PropertyBlockSelectorNode.parse(parser, result, MethodSelectorAnalyzer.signalEndArgument)
            if (argument == null && parser.readText(selectorStartToken)) {
                WhitespaceNode.parse(parser)

                argument = SelectorNode.parse(parser, result, MethodSelectorAnalyzer.signalEndArgument) ?: let {
                    val expressionCursor = parser.reader.saveCursor()

                    // To show the end token in the message if it exists.
                    parser.readText(NameSelectorNode.groupEndToken)

                    throw AngmarParserException(AngmarParserExceptionType.SelectorMethodArgumentsWithoutCondition,
                            "Selector method arguments require an expression to act as its condition.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(expressionCursor.position())
                            message = "Try adding an expression here"
                        }
                    }
                }

                WhitespaceNode.parse(parser)

                if (!parser.readText(selectorEndToken)) {
                    throw AngmarParserException(AngmarParserExceptionType.SelectorMethodArgumentsWithoutEndToken,
                            "Selector method arguments require the close parenthesis '$selectorEndToken'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding the close parenthesis '$selectorEndToken' here"
                        }
                    }
                }
            }

            result.argument = argument

            return parser.finalizeNode(result, initCursor)
        }
    }
}
