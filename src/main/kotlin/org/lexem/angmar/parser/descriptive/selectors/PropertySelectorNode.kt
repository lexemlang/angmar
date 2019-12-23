package org.lexem.angmar.parser.descriptive.selectors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.selectors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for properties of selectors.
 */
internal class PropertySelectorNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var isAddition = false
    val properties = mutableListOf<PropertyAbbreviationSelectorNode>()

    override fun toString() = StringBuilder().apply {
        append(token)

        if (properties.size == 1) {
            append(properties.first())
        } else {
            append(groupStartToken)
            append(properties.joinToString("$elementSeparator "))
            append(groupEndToken)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isAddition", isAddition)
        result.add("properties", SerializationUtils.listToTest(properties))

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            PropertySelectorCompiled.compile(parent, parentSignal, this)

    companion object {
        const val token = "."
        const val groupStartToken = ParenthesisExpressionNode.startToken
        const val groupEndToken = ParenthesisExpressionNode.endToken
        const val elementSeparator = GlobalCommons.elementSeparator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a property of a selector.
         */
        fun parse(parser: LexemParser, parent: ParserNode): PropertySelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PropertySelectorNode(parser, parent)

            if (!parser.readText(token)) {
                return null
            }

            if (parser.readText(groupStartToken)) {
                WhitespaceNode.parse(parser)

                while (true) {
                    val initIterationCursor = parser.reader.saveCursor()

                    if (result.properties.isNotEmpty()) {
                        if (!parser.readText(elementSeparator)) {
                            break
                        }

                        WhitespaceNode.parse(parser)
                    }

                    val property = PropertyAbbreviationSelectorNode.parse(parser, result)
                    if (property == null) {
                        initIterationCursor.restore()
                        break
                    }

                    result.properties.add(property)

                    WhitespaceNode.parse(parser)
                }

                if (result.properties.isEmpty()) {
                    val propertyCursor = parser.reader.saveCursor()

                    // To show the end token in the message if it exists.
                    parser.readText(NameSelectorNode.groupEndToken)

                    throw AngmarParserException(AngmarParserExceptionType.SelectorPropertyGroupWithoutProperties,
                            "Selector property groups require at least one property.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(propertyCursor.position())
                            message = "Try adding a property here"
                        }
                    }
                }

                if (!parser.readText(groupEndToken)) {
                    throw AngmarParserException(AngmarParserExceptionType.SelectorPropertyGroupWithoutEndToken,
                            "Selector property groups require the close parenthesis '$groupEndToken'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding the close parenthesis '$groupEndToken' here"
                        }
                    }
                }
            } else {
                val property = PropertyAbbreviationSelectorNode.parse(parser, result) ?: throw AngmarParserException(
                        AngmarParserExceptionType.SelectorPropertyWithoutIdentifier,
                        "Selector properties require a property group or identifier after the start token '$token'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding an identifier or property group here"
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        message = "Try removing the start token '$token'"
                    }
                }

                result.properties.add(property)
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a property of a selector for an Addition.
         */
        fun parseForAddition(parser: LexemParser, parent: ParserNode): PropertySelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PropertySelectorNode(parser, parent)
            result.isAddition = true

            if (!parser.readText(token)) {
                return null
            }

            val property =
                    PropertyAbbreviationSelectorNode.parseForAddition(parser, result) ?: throw AngmarParserException(
                            AngmarParserExceptionType.SelectorPropertyWithoutIdentifier,
                            "Selector properties require a property group or identifier after the start token '$token'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding an identifier or property group here"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            message = "Try removing the start token '$token'"
                        }
                    }

            result.properties.add(property)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
