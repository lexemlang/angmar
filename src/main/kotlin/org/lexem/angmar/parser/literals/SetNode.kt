package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for set literals
 */
internal class SetNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var list: ListNode

    override fun toString() = list.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("list", list.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = SetAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val macroName = "set${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a set literal
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): SetNode? {
            val initCursor = parser.reader.saveCursor()
            val result = SetNode(parser, parent, parentSignal)

            if (!parser.readText(macroName)) {
                return null
            }

            result.list = ListNode.parse(parser, result, SetAnalyzer.signalEndList) ?: throw AngmarParserException(
                    AngmarParserExceptionType.SetWithoutStartToken,
                    "The open square bracket was expected '${ListNode.startToken}' after the macro name '$macroName'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message = "Try adding the open square bracket '${ListNode.startToken}' here"
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message = "Try removing the macro name '$macroName'"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
