package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for accesses expression.
 */
internal class AccessExpressionNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var element: ParserNode
    var modifiers = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(element)
        modifiers.forEach { append(it) }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("element", element.toTree())
        result.add("modifiers", SerializationUtils.listToTest(modifiers))

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            AccessExpressionCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses an access expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode): ParserNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AccessExpressionNode(parser, parent)

            result.element =
                    ExpressionsCommons.parseLiteral(parser, result) ?: ExpressionsCommons.parseMacro(parser, result)
                            ?: ParenthesisExpressionNode.parse(parser, result) ?: IdentifierNode.parse(parser, result)
                            ?: return null

            while (true) {
                result.modifiers.add(AccessExplicitMemberNode.parse(parser, result) ?: IndexerNode.parse(parser, result)
                ?: FunctionCallNode.parse(parser, result) ?: break)
            }

            if (result.modifiers.isEmpty() && result.element !is IdentifierNode) {
                val newResult = result.element
                newResult.parent = parent
                return newResult
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
