package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for binary sequence lexemes.
 */
internal class BinarySequenceLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
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

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            BinarySequenceLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a bitlist lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode): BinarySequenceLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = BinarySequenceLexemeNode(parser, parent)

            result.isNegated = parser.readText(notOperator)

            val bitlist = BitlistNode.parse(parser, result)
            if (bitlist == null) {
                initCursor.restore()
                return null
            }

            result.bitlist = bitlist

            result.propertyPostfix = LexemPropertyPostfixNode.parse(parser, result)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
