package org.lexem.angmar.parser.descriptive.selectors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.selectors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for property blocks of selectors.
 */
internal class PropertyBlockSelectorNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
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

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            PropertyBlockSelectorCompiled.compile(parent, parentSignal, this)

    companion object {
        const val startToken = "["
        const val endToken = "]"
        const val relationalToken = GlobalCommons.relationalToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a property block of a selector.
         */
        fun parse(parser: LexemParser, parent: ParserNode): PropertyBlockSelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PropertyBlockSelectorNode(parser, parent)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            // Identifier
            let {
                val preIdentifierCursor = parser.reader.saveCursor()
                val identifier = IdentifierNode.parse(parser, result) ?: return@let

                WhitespaceNode.parse(parser)

                if (!parser.readText(relationalToken)) {
                    preIdentifierCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.identifier = identifier
            }

            result.condition = ExpressionsCommons.parseExpression(parser, result) ?: let {
                val expressionCursor = parser.reader.saveCursor()

                // To show the end token in the message if it exists.
                parser.readText(endToken)

                throw AngmarParserException(AngmarParserExceptionType.SelectorPropertyBlockWithoutCondition,
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
        fun parseForAddition(parser: LexemParser, parent: ParserNode): PropertyBlockSelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PropertyBlockSelectorNode(parser, parent)
            result.isAddition = true

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            result.condition = ExpressionsCommons.parseExpression(parser, result) ?: let {
                val expressionCursor = parser.reader.saveCursor()

                // To show the end token in the message if it exists.
                parser.readText(endToken)

                throw AngmarParserException(AngmarParserExceptionType.SelectorPropertyBlockWithoutCondition,
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
    }
}
