package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for assign expressions.
 */
internal class AssignExpressionNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var left: ParserNode
    lateinit var operator: AssignOperatorNode
    lateinit var right: RightExpressionNode

    override fun toString() = StringBuilder().apply {
        append(left)
        append(' ')
        append(operator)
        append(' ')
        append(right)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("left", left.toTree())
        result.add("operator", operator.toTree())
        result.add("right", right.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            AssignExpressionCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses an assign expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode): AssignExpressionNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AssignExpressionNode(parser, parent)

            result.left = ExpressionsCommons.parseLeftExpression(parser, result) ?: return null

            WhitespaceNoEOLNode.parse(parser)

            result.operator = AssignOperatorNode.parse(parser, result) ?: let {
                initCursor.restore()
                return@parse null
            }

            WhitespaceNode.parse(parser)

            result.right = RightExpressionNode.parse(parser, result) ?: let {
                throw AngmarParserException(
                        AngmarParserExceptionType.AssignExpressionWithoutExpressionAfterAssignOperator,
                        "An expression was expected after the assign operator '${result.operator}'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(result.operator.from.position(), result.operator.to.position())
                        message = "Try removing the assign operator"
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(result.operator.to.position())
                        message = "Try adding an expression after the operator"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
