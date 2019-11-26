package org.lexem.angmar.parser.descriptive

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for accesses expression for data capturing.
 */
internal class DataCapturingAccessLexemeNode private constructor(parser: LexemParser, parent: ParserNode,
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
            DataCapturingAccessLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses an access expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): DataCapturingAccessLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), DataCapturingAccessLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = DataCapturingAccessLexemeNode(parser, parent, parentSignal)

            result.element = IdentifierNode.parse(parser, result, DataCapturingAccessLexemeAnalyzer.signalEndElement)
                    ?: return null

            while (true) {
                result.modifiers.add(AccessExplicitMemberNode.parse(parser, result,
                        result.modifiers.size + DataCapturingAccessLexemeAnalyzer.signalEndFirstModifier)
                        ?: IndexerNode.parse(parser, result,
                                result.modifiers.size + DataCapturingAccessLexemeAnalyzer.signalEndFirstModifier)
                        ?: FunctionCallNode.parse(parser, result,
                                result.modifiers.size + DataCapturingAccessLexemeAnalyzer.signalEndFirstModifier)
                        ?: break)
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
