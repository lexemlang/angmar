package org.lexem.angmar.parser.functional.expressions.macros

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.macros.*
import org.lexem.angmar.parser.*


/**
 * Parser for macro expressions.
 */
internal class MacroExpressionNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var macroName: String

    override fun toString() = StringBuilder().apply {
        append(macroName)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("macroName", macroName)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            MacroExpressionAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val macroSuffix = "!"
        const val lineMacro = "line$macroSuffix"
        const val fileMacro = "file$macroSuffix"
        const val directoryMacro = "directory$macroSuffix"
        const val fileContentMacro = "file_content$macroSuffix"
        val macroNames = listOf(lineMacro, fileMacro, directoryMacro, fileContentMacro)

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): MacroExpressionNode? {
            parser.fromBuffer(parser.reader.currentPosition(), MacroExpressionNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = MacroExpressionNode(parser, parent, parentSignal)

            result.macroName = parser.readAnyText(macroNames.asSequence()) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
