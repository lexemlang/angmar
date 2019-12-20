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
 * Parser for key-value pairs of object literals.
 */
internal class ObjectElementNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var key: ParserNode
    lateinit var value: ParserNode
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        if (isConstant) {
            append(constantToken)
        }
        append(key)
        append(keyValueSeparator)
        append(' ')
        append(value)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("key", key.toTree())
        result.add("value", value.toTree())
        result.addProperty("isConstant", isConstant)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ObjectElementAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val constantToken = GlobalCommons.constantToken
        const val keyValueSeparator = GlobalCommons.relationalToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses a key-value pair of object literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ObjectElementNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ObjectElementNode(parser, parent, parentSignal)

            result.isConstant = parser.readText(constantToken)
            result.key = Commons.parseDynamicIdentifier(parser, result, ObjectElementAnalyzer.signalEndKey) ?: let {
                if (result.isConstant) {
                    throw AngmarParserException(AngmarParserExceptionType.ObjectElementWithoutKeyAfterConstantToken,
                            "A key was expected after the constant token '$constantToken'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(parser.reader.currentPosition() - 1)
                            message = "Try removing the constant token here"
                        }
                    }
                }

                initCursor.restore()
                return@parse null
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(keyValueSeparator)) {
                throw AngmarParserException(AngmarParserExceptionType.ObjectElementWithoutRelationalOperatorAfterKey,
                        "The relational separator '$keyValueSeparator' was expected after the key.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the relational separator '$keyValueSeparator' here"
                    }
                }
            }

            WhitespaceNode.parse(parser)

            result.value = ExpressionsCommons.parseExpression(parser, result, ObjectElementAnalyzer.signalEndValue)
                    ?: throw AngmarParserException(
                            AngmarParserExceptionType.ObjectElementWithoutExpressionAfterRelationalOperator,
                            "An expression acting as value was expected after the relational separator '$keyValueSeparator'.") {
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

            return parser.finalizeNode(result, initCursor)
        }
    }
}
