package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Parser for right expressions i.e. expressions that returns a value.
 */
internal class RightExpressionNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var expression: ParserNode

    override fun toString() = expression.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            RightExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses a right expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): RightExpressionNode? {
            parser.fromBuffer(parser.reader.currentPosition(), RightExpressionNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = RightExpressionNode(parser, parent, parentSignal)

            result.expression =
                    ConditionalExpressionNode.parse(parser, result, RightExpressionAnalyzer.signalEndExpression)
                            ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
