package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for binary sequence lexemes.
 */
internal class BinarySequenceLexemeNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    var propertyPostfix: LexemPropertyPostfixNode? = null
    lateinit var bitlist: BitlistNode

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(bitlist)

        if (propertyPostfix != null) {
            append(propertyPostfix)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("propertyPostfix", propertyPostfix?.toTree())
        result.add("bitlist", bitlist.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            BinarySequenceLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a bitlist lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): BinarySequenceLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), BinarySequenceLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = BinarySequenceLexemeNode(parser, parent, parentSignal)

            result.isNegated = parser.readText(notOperator)

            val bitlist = BitlistNode.parse(parser, result, BinarySequenceLexemAnalyzer.signalEndBitlist)
            if (bitlist == null) {
                initCursor.restore()
                return null
            }

            result.bitlist = bitlist

            result.propertyPostfix =
                    LexemPropertyPostfixNode.parse(parser, result, BinarySequenceLexemAnalyzer.signalEndPropertyPostfix)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
