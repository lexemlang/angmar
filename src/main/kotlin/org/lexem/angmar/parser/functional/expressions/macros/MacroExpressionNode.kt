package org.lexem.angmar.parser.functional.expressions.macros

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.macros.*
import org.lexem.angmar.parser.*


/**
 * Parser for macro expressions.
 */
internal class MacroExpressionNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var macroName: String

    override fun toString() = StringBuilder().apply {
        append(macroName)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("macroName", macroName)

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            MacroExpressionCompiler.compile(parent, parentSignal, this)

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
        fun parse(parser: LexemParser, parent: ParserNode): MacroExpressionNode? {
            val initCursor = parser.reader.saveCursor()
            val result = MacroExpressionNode(parser, parent)

            result.macroName = parser.readAnyText(macroNames.asSequence()) ?: return null

            return parser.finalizeNode(result, initCursor)
        }
    }
}
