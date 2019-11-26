package org.lexem.angmar.parser.functional.expressions.binary

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for conditional expressions.
 */
internal class ConditionalExpressionNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    val expressions = mutableListOf<ParserNode>()
    val operators = mutableListOf<String>()

    override fun toString() = StringBuilder().apply {
        append(expressions.first())
        for (i in 1 until expressions.size) {
            append(" ${operators[i - 1]} ")
            append(expressions[i])
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expressions", SerializationUtils.listToTest(expressions))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ConditionalExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val andOperator = "and"
        const val orOperator = "or"
        const val xorOperator = "xor"
        val operators = listOf(andOperator, xorOperator, orOperator)

        // METHODS ------------------------------------------------------------

        /**
         * Parses a conditional expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ParserNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ConditionalExpressionNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ConditionalExpressionNode(parser, parent, parentSignal)

            result.expressions.add(RelationalExpressionNode.parse(parser, result,
                    result.expressions.size + ConditionalExpressionAnalyzer.signalEndFirstExpression) ?: return null)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNoEOLNode.parse(parser)

                val preOperatorCursor = parser.reader.saveCursor()
                val operator = when {
                    Commons.parseKeyword(parser, andOperator) -> andOperator
                    Commons.parseKeyword(parser, xorOperator) -> xorOperator
                    Commons.parseKeyword(parser, orOperator) -> orOperator
                    else -> null
                }

                if (operator == null) {
                    initLoopCursor.restore()
                    break
                }

                result.operators.add(operator)

                WhitespaceNode.parse(parser)

                val expression = RelationalExpressionNode.parse(parser, result,
                        result.expressions.size + ConditionalExpressionAnalyzer.signalEndFirstExpression) ?: let {
                    throw AngmarParserException(
                            AngmarParserExceptionType.ConditionalExpressionWithoutExpressionAfterOperator,
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
