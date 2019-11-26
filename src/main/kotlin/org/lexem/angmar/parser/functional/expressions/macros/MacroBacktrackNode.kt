package org.lexem.angmar.parser.functional.expressions.macros

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.macros.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for macro 'backtrack'.
 */
internal class MacroBacktrackNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var arguments: FunctionCallNode? = null

    override fun toString() = StringBuilder().apply {
        append(macroName)

        if (arguments != null) {
            append(arguments)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("arguments", arguments?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            MacroBacktrackAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val macroName = "backtrack${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro 'backtrack'.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): MacroBacktrackNode? {
            parser.fromBuffer(parser.reader.currentPosition(), MacroBacktrackNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val result = MacroBacktrackNode(parser, parent, parentSignal)

            result.arguments = FunctionCallNode.parse(parser, result, MacroBacktrackAnalyzer.signalEndValue)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
