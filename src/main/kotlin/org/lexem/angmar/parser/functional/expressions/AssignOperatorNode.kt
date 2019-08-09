package org.lexem.angmar.parser.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Parser for assign operators.
 */
class AssignOperatorNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var operator = ""

    override fun toString() = "$operator="

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("operator", operator)
    }

    companion object {
        const val assignOperator = "="
        val operators =
                (ShiftExpressionNode.operators.asSequence() + MultiplicativeExpressionNode.operators + AdditiveExpressionNode.operators + LogicalExpressionNode.operators + ConditionalExpressionNode.operators).map { "$it$assignOperator" } + assignOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses an assign operator.
         */
        fun parse(parser: LexemParser): AssignOperatorNode? {
            parser.fromBuffer(parser.reader.currentPosition(), AssignOperatorNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = AssignOperatorNode(parser)

            result.operator =
                    ExpressionsCommons.readOperator(parser, operators)?.removeSuffix(assignOperator) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
