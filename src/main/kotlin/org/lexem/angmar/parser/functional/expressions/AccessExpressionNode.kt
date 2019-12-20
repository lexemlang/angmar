package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.AccessExpressionAnalyzer.signalEndFirstModifier
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for accesses expression.
 */
internal class AccessExpressionNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
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

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AccessExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses an access expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ParserNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AccessExpressionNode(parser, parent, parentSignal)

            result.element = ExpressionsCommons.parseLiteral(parser, result, AccessExpressionAnalyzer.signalEndElement)
                    ?: ExpressionsCommons.parseMacro(parser, result, AccessExpressionAnalyzer.signalEndElement)
                            ?: ParenthesisExpressionNode.parse(parser, result,
                            AccessExpressionAnalyzer.signalEndElement) ?: IdentifierNode.parse(parser, result,
                            AccessExpressionAnalyzer.signalEndElement) ?: return null

            while (true) {
                result.modifiers.add(
                        AccessExplicitMemberNode.parse(parser, result, result.modifiers.size + signalEndFirstModifier)
                                ?: IndexerNode.parse(parser, result, result.modifiers.size + signalEndFirstModifier)
                                ?: FunctionCallNode.parse(parser, result,
                                        result.modifiers.size + signalEndFirstModifier) ?: break)
            }

            if (result.modifiers.isEmpty() && result.element !is IdentifierNode) {
                val newResult = result.element
                newResult.parent = parent
                newResult.parentSignal = parentSignal
                return newResult
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
