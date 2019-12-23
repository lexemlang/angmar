package org.lexem.angmar.parser.functional.expressions.binary

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.binary.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for relational expressions.
 */
internal class RelationalExpressionNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
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

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            RelationalExpressionCompiled.compile(parent, parentSignal, this)

    companion object {
        const val identityOperator = "==="
        const val notIdentityOperator = "!=="
        const val equalityOperator = "=="
        const val inequalityOperator = "!="
        const val lowerThanOperator = "<"
        const val lowerOrEqualThanOperator = "<="
        const val greaterThanOperator = ">"
        const val greaterOrEqualThanOperator = ">="
        val operators = listOf(identityOperator, notIdentityOperator, equalityOperator, inequalityOperator,
                lowerOrEqualThanOperator, greaterOrEqualThanOperator, lowerThanOperator, greaterThanOperator)

        // METHODS ------------------------------------------------------------

        /**
         * Parses a relational expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode): ParserNode? {
            val initCursor = parser.reader.saveCursor()
            val result = RelationalExpressionNode(parser, parent)

            result.expressions.add(LogicalExpressionNode.parse(parser, result) ?: return null)

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

                val expression = LogicalExpressionNode.parse(parser, result) ?: let {
                    throw AngmarParserException(
                            AngmarParserExceptionType.RelationalExpressionWithoutExpressionAfterOperator,
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
                return newResult
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
