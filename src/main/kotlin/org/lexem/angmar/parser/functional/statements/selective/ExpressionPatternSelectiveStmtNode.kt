package org.lexem.angmar.parser.functional.statements.selective

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.selective.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for expression patterns of the selective statements.
 */
internal class ExpressionPatternSelectiveStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var expression: ParserNode
    var conditional: ConditionalPatternSelectiveStmtNode? = null

    override fun toString() = StringBuilder().apply {
        append(expression)
        if (conditional != null) {
            append(' ')
            append(conditional)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())
        result.add("conditional", conditional?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            ExpressionPatternSelectiveStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses a expression pattern of the selective statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode): ExpressionPatternSelectiveStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ExpressionPatternSelectiveStmtNode(parser, parent)

            result.expression = ExpressionsCommons.parseExpression(parser, result) ?: return null

            // conditional
            let {
                val initConditionalCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                result.conditional = ConditionalPatternSelectiveStmtNode.parse(parser, result)
                if (result.conditional == null) {
                    initConditionalCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
