package org.lexem.angmar.parser.functional.expressions.macros

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for macro 'backtrack'.
 */
internal class MacroBacktrack private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var value: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(macroName)

        if (value != null) {
            append(value)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("value", value?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) {
        TODO("not implemented analyzer")
    }

    companion object {
        const val signalEndValue = 1
        const val macroName = "backtrack${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro 'backtrack'.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): MacroBacktrack? {
            parser.fromBuffer(parser.reader.currentPosition(), MacroBacktrack::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val result = MacroBacktrack(parser, parent, parentSignal)

            result.value = FunctionCallNode.parse(parser, result, signalEndValue)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
