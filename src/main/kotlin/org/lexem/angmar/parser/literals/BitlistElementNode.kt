package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for an element of the bitlist literals.
 */
internal class BitlistElementNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var radix = 0
    var number: String? = null
    var expression: EscapedExpressionNode? = null

    override fun toString() = if (number != null) {
        number!!
    } else {
        expression.toString()
    }

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("number", number)
        result.add("expression", expression?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            BitListElementAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses an element of the bitlist literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int, radix: Int): BitlistElementNode? {
            parser.fromBuffer(parser.reader.currentPosition(), BitlistElementNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = BitlistElementNode(parser, parent, parentSignal)
            result.radix = radix

            result.expression = EscapedExpressionNode.parse(parser, result, BitListElementAnalyzer.signalEndExpression)
            if (result.expression == null) {
                result.number = when (radix) {
                    2 -> NumberNode.readBinaryInteger(parser)
                    8 -> NumberNode.readOctalInteger(parser)
                    16 -> NumberNode.readHexadecimalInteger(parser)
                    else -> throw AngmarUnreachableException()
                }

                if (result.number == null) {
                    return null
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
