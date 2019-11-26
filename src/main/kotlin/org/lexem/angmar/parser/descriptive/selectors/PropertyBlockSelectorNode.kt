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
 * Parser for property blocks of selectors.
 */
internal class PropertyBlockSelectorNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    var isAddition = false
    var identifier: IdentifierNode? = null
    lateinit var condition: ParserNode

    override fun toString() = StringBuilder().apply {
        append(startToken)

        if (identifier != null) {
            append(identifier)
            append(relationalToken)
            append(" ")
        }

        append(condition)
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isAddition", isAddition)
        result.add("identifier", identifier?.toTree())
        result.add("condition", condition.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyBlockSelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "["
        const val endToken = "]"
        const val relationalToken = GlobalCommons.relationalToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a property block of a selector.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): PropertyBlockSelectorNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PropertyBlockSelectorNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = PropertyBlockSelectorNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            // Identifier
            let {
                val preIdentifierCursor = parser.reader.saveCursor()
                val identifier = IdentifierNode.parse(parser, result, PropertyBlockSelectorAnalyzer.signalEndIdentifier)
                        ?: return@let

                WhitespaceNode.parse(parser)

                if (!parser.readText(relationalToken)) {
                    preIdentifierCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.identifier = identifier
            }

            result.condition =
                    ExpressionsCommons.parseExpression(parser, result, PropertyBlockSelectorAnalyzer.signalEndCondition)
                            ?: let {
                                val expressionCursor = parser.reader.saveCursor()

                                // To show the end token in the message if it exists.
                                parser.readText(endToken)

                                throw AngmarParserException(
                                        AngmarParserExceptionType.SelectorPropertyBlockWithoutCondition,
                                        "Selector property block require an expression to act as its condition.") {
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

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.SelectorPropertyBlockWithoutEndToken,
                        "Selector property blocks require the close square bracket '$endToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close square bracket '$endToken' here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a property block of a selector for an Addition.
         */
        fun parseForAddition(parser: LexemParser, parent: ParserNode, parentSignal: Int): PropertyBlockSelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PropertyBlockSelectorNode(parser, parent, parentSignal)
            result.isAddition = true

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            result.condition =
                    ExpressionsCommons.parseExpression(parser, result, PropertyBlockSelectorAnalyzer.signalEndCondition)
                            ?: let {
                                val expressionCursor = parser.reader.saveCursor()

                                // To show the end token in the message if it exists.
                                parser.readText(endToken)

                                throw AngmarParserException(
                                        AngmarParserExceptionType.SelectorPropertyBlockWithoutCondition,
                                        "Selector property block require an expression to act as its condition.") {
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

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.SelectorPropertyBlockWithoutEndToken,
                        "Selector property blocks require the close square bracket '$endToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close square bracket '$endToken' here"
                    }
                }
            }

            return parser.finalizeNodeNoBuffer(result, initCursor)
        }
    }
}
