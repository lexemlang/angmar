package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for block lexemes.
 */
internal class BlockLexemeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    var propertyPostfix: LexemPropertyPostfixNode? = null
    lateinit var block: ParserNode

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(block)

        if (propertyPostfix != null) {
            append(propertyPostfix)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("propertyPostfix", propertyPostfix?.toTree())
        result.add("block", block.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = BlockLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a block lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): BlockLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = BlockLexemeNode(parser, parent, parentSignal)

            result.isNegated = parser.readText(notOperator)

            val block = LiteralCommons.parseAnyIntervalForLexem(parser, result, BlockLexemAnalyzer.signalEndInterval)
            if (block == null) {
                initCursor.restore()
                return null
            }

            result.block = block

            result.propertyPostfix =
                    LexemPropertyPostfixNode.parse(parser, result, BlockLexemAnalyzer.signalEndPropertyPostfix)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
