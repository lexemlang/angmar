package org.lexem.angmar.parser.descriptive

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for lexeme pattern contents.
 */
internal class LexemePatternContentNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var lexemes = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(lexemes.joinToString(" "))
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("lexemes", SerializationUtils.listToTest(lexemes))

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            LexemePatternContentCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses a Lexem pattern content.
         */
        fun parse(parser: LexemParser, parent: ParserNode): LexemePatternContentNode? {
            val initCursor = parser.reader.saveCursor()
            val result = LexemePatternContentNode(parser, parent)

            var first = true
            while (true) {
                val iterationCursor = parser.reader.saveCursor()
                if (!first) {
                    PatternWhitespaceNode.parse(parser)
                }

                val lexeme = GlobalCommons.parseLexem(parser, result)

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
