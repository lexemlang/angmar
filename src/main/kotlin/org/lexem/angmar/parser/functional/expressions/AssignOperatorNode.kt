package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Parser for assign operators.
 */
internal class AssignOperatorNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var operator = ""

    override fun toString() = "$operator$assignOperator"

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("operator", operator)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AssignOperatorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val assignOperator = "="
        val operators =
                (ShiftExpressionNode.operators.asSequence() + MultiplicativeExpressionNode.operators + AdditiveExpressionNode.operators + LogicalExpressionNode.operators + ConditionalExpressionNode.operators).map { "$it$assignOperator" }
        val assignOperatorSequence = listOf(assignOperator).asSequence()
        val assignOperatorSkipSuffix = listOf(assignOperator)

        // METHODS ------------------------------------------------------------

        /**
         * Parses an assign operator.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): AssignOperatorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AssignOperatorNode(parser, parent, parentSignal)

            result.operator = ExpressionsCommons.readOperator(parser, operators)?.removeSuffix(assignOperator)
                    ?: ExpressionsCommons.readOperator(parser, assignOperatorSequence,
                            assignOperatorSkipSuffix)?.removeSuffix(assignOperator) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
