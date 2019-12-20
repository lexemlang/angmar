package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for interval literals.
 */
internal class IntervalNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    val elements = mutableListOf<ParserNode>()
    var reversed = false

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(startToken)
        if (reversed) {
            append(reversedToken)
        }
        append(elements.joinToString(" "))
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("elements", SerializationUtils.listToTest(elements))
        result.addProperty("reversed", reversed)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = IntervalAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val macroName = "itv${MacroExpressionNode.macroSuffix}"
        const val startToken = "["
        const val reversedToken = GlobalCommons.notToken
        const val endToken = "]"


        // METHODS ------------------------------------------------------------

        /**
         * Parses an interval literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): IntervalNode? {
            val initCursor = parser.reader.saveCursor()
            val result = IntervalNode(parser, parent, parentSignal)

            if (!parser.readText(macroName)) {
                return null
            }

            if (!parser.readText(startToken)) {
                throw AngmarParserException(AngmarParserExceptionType.IntervalWithoutStartToken,
                        "The start square bracket was expected '$startToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the start and end square brackets '$startToken$endToken' here"
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        message = "Try removing the '$macroName' macro"
                    }
                }
            }

            result.reversed = parser.readText(reversedToken)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parseSimpleWhitespaces(parser)

                val node = IntervalSubIntervalNode.parse(parser, result,
                        result.elements.size + IntervalAnalyzer.signalEndFirstElement) ?: IntervalElementNode.parse(
                        parser, result, result.elements.size + IntervalAnalyzer.signalEndFirstElement)
                if (node == null) {
                    initLoopCursor.restore()
                    break
                }

                result.elements.add(node)
            }

            WhitespaceNode.parseSimpleWhitespaces(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.IntervalWithoutEndToken,
                        "The close square bracket was expected '$endToken'.") {
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
