package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for block lexemes.
 */
internal class BlockLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var isNegated = false
    var propertyPostfix: LexemPropertyPostfixNode? = null
    lateinit var interval: ParserNode

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(interval)

        if (propertyPostfix != null) {
            append(propertyPostfix)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("propertyPostfix", propertyPostfix?.toTree())
        result.add("interval", interval.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            BlockLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a block lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode): BlockLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = BlockLexemeNode(parser, parent)

            result.isNegated = parser.readText(notOperator)

            val block = LiteralCommons.parseAnyIntervalForLexem(parser, result)
            if (block == null) {
                initCursor.restore()
                return null
            }

            result.interval = block

            result.propertyPostfix = LexemPropertyPostfixNode.parse(parser, result)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
