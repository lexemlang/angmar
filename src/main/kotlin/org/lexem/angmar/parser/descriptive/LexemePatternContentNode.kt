package org.lexem.angmar.parser.descriptive

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for lexeme pattern contents.
 */
internal class LexemePatternContentNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    var lexemes = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(lexemes.joinToString(" "))
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("lexemes", SerializationUtils.listToTest(lexemes))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            LexemePatternContentAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses a Lexem pattern content.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): LexemePatternContentNode? {
            parser.fromBuffer(parser.reader.currentPosition(), LexemePatternContentNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = LexemePatternContentNode(parser, parent, parentSignal)

            var first = true
            while (true) {
                val iterationCursor = parser.reader.saveCursor()
                if (!first) {
                    PatternWhitespaceNode.parse(parser)
                }

                val lexeme = GlobalCommons.parseLexem(parser, result,
                        result.lexemes.size + LexemePatternContentAnalyzer.signalEndFirstLexeme)

                if (lexeme == null) {
                    iterationCursor.restore()
                    break
                }

                result.lexemes.add(lexeme)
                first = false
            }

            if (result.lexemes.isEmpty()) {
                initCursor.restore()
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
