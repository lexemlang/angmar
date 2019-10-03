package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for property-style object element.
 */
internal class PropertyStyleObjectElementNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var key: ParserNode
    lateinit var value: ParenthesisExpressionNode

    override fun toString() = StringBuilder().apply {
        append(key)
        append(value)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("key", key.toTree())
        result.add("value", value.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyStyleObjectElementAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses a property-style object element.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): PropertyStyleObjectElementNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PropertyStyleObjectElementNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = PropertyStyleObjectElementNode(parser, parent, parentSignal)

            result.key = Commons.parseDynamicIdentifier(parser, result, PropertyStyleObjectElementAnalyzer.signalEndKey)
                    ?: return null
            result.value =
                    ParenthesisExpressionNode.parse(parser, result, PropertyStyleObjectElementAnalyzer.signalEndValue)
                            ?: throw AngmarParserException(
                                    AngmarParserExceptionType.PropertyStyleObjectElementWithoutExpressionAfterName,
                                    "An parenthesized expression was expected after the name of the property.") {
                                val fullText = parser.reader.readAllText()
                                addSourceCode(fullText, parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightCursorAt(parser.reader.currentPosition())
                                    message = "Try adding a value here e.g. '(value)'"
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    message = "Try removing the name of the property"
                                }
                            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
