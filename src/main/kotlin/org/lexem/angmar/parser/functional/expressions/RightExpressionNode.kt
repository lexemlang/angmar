package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Parser for right expressions i.e. expressions that returns a value.
 */
internal class RightExpressionNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var expression: ParserNode

    override fun toString() = expression.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            RightExpressionCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses a right expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode): RightExpressionNode? {
            val initCursor = parser.reader.saveCursor()
            val result = RightExpressionNode(parser, parent)

            result.expression = ConditionalExpressionNode.parse(parser, result) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
