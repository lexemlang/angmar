package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for functional expression statements.
 */
internal class FunctionalExpressionStmtNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var expression: ParserNode

    override fun toString() = expression.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionalExpressionStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses an expression statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): FunctionalExpressionStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionalExpressionStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = FunctionalExpressionStmtNode(parser, parent, parentSignal)

            result.expression = ExpressionsCommons.parseExpression(parser, result,
                    FunctionalExpressionStmtAnalyzer.signalEndExpression) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
