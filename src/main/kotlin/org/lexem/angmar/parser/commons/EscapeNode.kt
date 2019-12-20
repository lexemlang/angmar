package org.lexem.angmar.parser.commons

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.commons.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*

/**
 * Parser for escaped characters.
 */
internal class EscapeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var value: String

    override fun toString() = "$startToken$value"

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("value", value)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = EscapeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        internal const val startToken = "\\"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an escape.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): EscapeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = EscapeNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                return null
            }

            result.value = WhitespaceNode.readLineBreak(parser).takeIf { it != null && it != "" }
                    ?: parser.reader.currentChar()?.toString() ?: throw AngmarParserException(
                            AngmarParserExceptionType.EscapeWithoutCharacter,
                            "Escapes require a character after the escape token '$startToken' but the end-of-file (EOF) was encountered.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(initCursor.position() + 1)
                            message = "Try adding a character here"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(initCursor.position())
                            message = "Try removing the escape token '$startToken' here"
                        }
                    }


            parser.reader.advance()
            return parser.finalizeNode(result, initCursor)
        }
    }
}
