package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for functional expression statements.
 */
internal class FunctionalExpressionStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var expression: ParserNode

    override fun toString() = expression.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            FunctionalExpressionStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses an expression statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode): FunctionalExpressionStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = FunctionalExpressionStmtNode(parser, parent)

            result.expression = ExpressionsCommons.parseExpression(parser, result) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
