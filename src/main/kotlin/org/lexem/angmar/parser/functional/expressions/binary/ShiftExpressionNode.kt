package org.lexem.angmar.parser.functional.expressions.binary

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for shift expressions.
 */
internal class ShiftExpressionNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    val expressions = mutableListOf<ParserNode>()
    val operators = mutableListOf<String>()

    override fun toString() = StringBuilder().apply {
        append(expressions.first())
        for (i in 1 until expressions.size) {
            append(operators[i - 1])
            append(expressions[i])
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expressions", SerializationUtils.listToTest(expressions))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ShiftExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val leftShiftOperator = "<<"
        const val rightShiftOperator = ">>"
        const val leftRotationOperator = "</"
        const val rightRotationOperator = "/>"
        val operators = listOf(leftShiftOperator, rightShiftOperator, leftRotationOperator, rightRotationOperator)

        // METHODS ------------------------------------------------------------

        /**
         * Parses an shift expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ParserNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ShiftExpressionNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ShiftExpressionNode(parser, parent, parentSignal)

            result.expressions.add(AdditiveExpressionNode.parse(parser, result,
                    result.expressions.size + ShiftExpressionAnalyzer.signalEndFirstExpression) ?: return null)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNoEOLNode.parse(parser)

                val preOperatorCursor = parser.reader.saveCursor()
                val operator = ExpressionsCommons.readOperator(parser, operators.asSequence())

                if (operator == null) {
                    initLoopCursor.restore()
                    break
                }

                result.operators.add(operator)

                WhitespaceNode.parse(parser)

                val expression = AdditiveExpressionNode.parse(parser, result,
                        result.expressions.size + ShiftExpressionAnalyzer.signalEndFirstExpression) ?: let {
                    throw AngmarParserException(AngmarParserExceptionType.ShiftExpressionWithoutExpressionAfterOperator,
                            "An expression was expected after the operator '$operator'") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(preOperatorCursor.position(),
                                    preOperatorCursor.position() + operator.length - 1)
                            message = "Try removing the operator '$operator'"
                        }
                    }
                }

                result.expressions.add(expression)
            }

            if (result.expressions.size == 1) {
                val newResult = result.expressions.first()
                newResult.parent = parent
                newResult.parentSignal = parentSignal
                return newResult
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
