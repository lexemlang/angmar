package org.lexem.angmar.parser.functional.expressions.macros

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.macros.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for macro 'backtrack'.
 */
internal class MacroBacktrackNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
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

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            MacroBacktrackCompiled.compile(parent, parentSignal, this)

    companion object {
        const val macroName = "backtrack${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro 'backtrack'.
         */
        fun parse(parser: LexemParser, parent: ParserNode): MacroBacktrackNode? {
            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val result = MacroBacktrackNode(parser, parent)

            result.arguments = FunctionCallNode.parse(parser, result)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
