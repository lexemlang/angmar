package org.lexem.angmar.parser.functional.expressions.modifiers

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for expression properties of function calls.
 */
internal class FunctionCallExpressionPropertiesNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var value: PropertyStyleObjectBlockNode

    override fun toString() = StringBuilder().apply {
        append(relationalToken)
        append(value)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("value", value.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionCallExpressionPropertiesAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val relationalToken = GlobalCommons.relationalToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses an expression properties of function calls.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): FunctionCallExpressionPropertiesNode? {
            val initCursor = parser.reader.saveCursor()
            val result = FunctionCallExpressionPropertiesNode(parser, parent, parentSignal)

            if (!parser.readText(relationalToken)) {
                return null
            }

            result.value = PropertyStyleObjectBlockNode.parse(parser, result,
                    FunctionCallExpressionPropertiesAnalyzer.signalEndValue) ?: throw AngmarParserException(
                    AngmarParserExceptionType.FunctionCallExpressionPropertiesWithoutPropertyStyleBlockAfterRelationalToken,
                    "A property-style block was expected after the relational token '$relationalToken'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message =
                            "Try adding an empty property-style block here '${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.endToken}'"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
