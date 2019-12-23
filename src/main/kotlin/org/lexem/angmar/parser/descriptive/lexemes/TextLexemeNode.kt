package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for text lexemes.
 */
internal class TextLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var isNegated = false
    var propertyPostfix: LexemPropertyPostfixNode? = null
    lateinit var text: StringNode

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(text)

        if (propertyPostfix != null) {
            append(propertyPostfix)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("propertyPostfix", propertyPostfix?.toTree())
        result.add("text", text.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            TextLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a text lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode): TextLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = TextLexemeNode(parser, parent)

            result.isNegated = parser.readText(notOperator)

            val text = StringNode.parse(parser, result)
            if (text == null) {
                initCursor.restore()
                return null
            }

            result.text = text

            result.propertyPostfix = LexemPropertyPostfixNode.parse(parser, result)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
