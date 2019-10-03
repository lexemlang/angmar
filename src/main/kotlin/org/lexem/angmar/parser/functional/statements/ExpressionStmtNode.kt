package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for expression statements.
 */
internal class ExpressionStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var expression: ParserNode

    override fun toString() = expression.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ExpressionStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses an expression statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ExpressionStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ExpressionStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ExpressionStmtNode(parser, parent, parentSignal)

            result.expression =
                    ExpressionsCommons.parseExpression(parser, result, ExpressionStmtAnalyzer.signalEndExpression)
                            ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
