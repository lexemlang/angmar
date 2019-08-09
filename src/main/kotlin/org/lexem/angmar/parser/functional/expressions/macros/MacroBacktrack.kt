package org.lexem.angmar.parser.functional.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for macro 'backtrack'.
 */
class MacroBacktrack private constructor(parser: LexemParser) : ParserNode(parser) {
    var value: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(macroName)

        if (value != null) {
            append(value)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addOptionalField("value", value)
    }

    companion object {
        const val macroName = "backtrack${MacroExpression.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro 'backtrack'.
         */
        fun parse(parser: LexemParser): MacroBacktrack? {
            parser.fromBuffer(parser.reader.currentPosition(), MacroBacktrack::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val result = MacroBacktrack(parser)

            result.value = FunctionCallNode.parse(parser)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
