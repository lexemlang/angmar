package org.lexem.angmar.parser.functional.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*


/**
 * Parser for macro expressions.
 */
class MacroExpression private constructor(parser: LexemParser, val macroName: String) : ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(macroName)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("macroName", macroName)
    }

    companion object {
        const val macroSuffix = "!"
        const val logicLineMacro = "logic_line$macroSuffix"
        val macroNames = listOf(logicLineMacro, "line$macroSuffix", "file$macroSuffix", "directory$macroSuffix",
                "file_content$macroSuffix")

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro expression.
         */
        fun parse(parser: LexemParser): MacroExpression? {
            parser.fromBuffer(parser.reader.currentPosition(), MacroExpression::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val macroName = parser.readAnyText(macroNames.asSequence()) ?: return null

            val result = MacroExpression(parser, macroName)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
