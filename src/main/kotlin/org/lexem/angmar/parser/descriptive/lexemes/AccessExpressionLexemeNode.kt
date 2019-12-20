package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for accesses expression for access lexemes.
 */
internal class AccessExpressionLexemeNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var element: IdentifierNode
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
            AccessExpressionLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses an access expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): AccessExpressionLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AccessExpressionLexemeNode(parser, parent, parentSignal)

            result.element =
                    IdentifierNode.parse(parser, result, AccessExpressionLexemeAnalyzer.signalEndElement) ?: return null

            while (true) {
                result.modifiers.add(AccessExplicitMemberNode.parse(parser, result,
                        result.modifiers.size + AccessExpressionLexemeAnalyzer.signalEndFirstModifier)
                        ?: IndexerNode.parse(parser, result,
                                result.modifiers.size + AccessExpressionLexemeAnalyzer.signalEndFirstModifier)
                        ?: FunctionCallNode.parse(parser, result,
                                result.modifiers.size + AccessExpressionLexemeAnalyzer.signalEndFirstModifier) ?: break)
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
