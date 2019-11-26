package org.lexem.angmar.parser.descriptive.selectors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for names of selectors.
 */
internal class NameSelectorNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    var isAddition = false
    val names = mutableListOf<IdentifierNode>()

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        if (names.size == 1) {
            append(names.first())
        } else {
            append(groupStartToken)
            append(names.joinToString("$elementSeparator "))
            append(groupEndToken)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.addProperty("isAddition", isAddition)
        result.add("names", SerializationUtils.listToTest(names))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            NameSelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val elementSeparator = GlobalCommons.elementSeparator
        const val groupStartToken = ParenthesisExpressionNode.startToken
        const val groupEndToken = ParenthesisExpressionNode.endToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a name of a selector.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): NameSelectorNode? {
            parser.fromBuffer(parser.reader.currentPosition(), NameSelectorNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = NameSelectorNode(parser, parent, parentSignal)

            result.isNegated = parser.readText(notOperator)

            if (parser.readText(groupStartToken)) {
                WhitespaceNode.parse(parser)

                while (true) {
                    val initIterationCursor = parser.reader.saveCursor()

                    if (result.names.isNotEmpty()) {
                        if (!parser.readText(elementSeparator)) {
                            break
                        }

                        WhitespaceNode.parse(parser)
                    }

                    val name = IdentifierNode.parse(parser, result,
                            result.names.size + NameSelectorAnalyzer.signalEndFirstName)
                    if (name == null) {
                        initIterationCursor.restore()
                        break
                    }

                    result.names.add(name)

                    WhitespaceNode.parse(parser)
                }

                if (result.names.isEmpty()) {
                    val identifierCursor = parser.reader.saveCursor()

                    // To show the end token in the message if it exists.
                    parser.readText(groupEndToken)

                    throw AngmarParserException(AngmarParserExceptionType.SelectorNameGroupWithoutNames,
                            "Selector name blocks require at least one name.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(identifierCursor.position())
                            message = "Try adding an identifier here"
                        }
                    }
                }

                if (!parser.readText(groupEndToken)) {
                    throw AngmarParserException(AngmarParserExceptionType.SelectorNameGroupWithoutEndToken,
                            "Selector name blocks require the close parenthesis '$groupEndToken'.") {
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
                val name = IdentifierNode.parse(parser, result, NameSelectorAnalyzer.signalEndFirstName)
                if (name == null) {
                    initCursor.restore()
                    return null
                }

                result.names.add(name)
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a name of a selector for an addition.
         */
        fun parseForAddition(parser: LexemParser, parent: ParserNode, parentSignal: Int): NameSelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = NameSelectorNode(parser, parent, parentSignal)
            result.isAddition = true

            val name = IdentifierNode.parse(parser, result, NameSelectorAnalyzer.signalEndFirstName)
            if (name == null) {
                initCursor.restore()
                return null
            }

            result.names.add(name)

            return parser.finalizeNodeNoBuffer(result, initCursor)
        }
    }
}
