package org.lexem.angmar.parser.descriptive.expressions.macros

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.expressions.macros.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for macro 'check props'.
 */
internal class CheckPropsMacroNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var properties: PropertyStyleObjectBlockNode

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(properties)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("properties", properties.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            CheckPropsMacroCompiled.compile(parent, parentSignal, this)

    companion object {
        const val signalEndValue = 1
        const val macroName = "check_props${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro 'check props'.
         */
        fun parse(parser: LexemParser, parent: ParserNode): CheckPropsMacroNode? {
            val initCursor = parser.reader.saveCursor()
            val result = CheckPropsMacroNode(parser, parent)

            if (!parser.readText(macroName)) {
                return null
            }

            result.properties = PropertyStyleObjectBlockNode.parse(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.MacroCheckPropsWithoutPropertyStyleBlockAfterMacroName,
                    "A property-style block was expected after the macro name '$macroName'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message =
                            "Try adding an empty property-style block here '${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.endToken}'"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
