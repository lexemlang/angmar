package org.lexem.angmar.parser.functional.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for macro 'check props'.
 */
class MacroCheckProps private constructor(parser: LexemParser, val value: PropertyStyleObjectBlockNode) :
        ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(value)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("value", value)
    }

    companion object {
        const val macroName = "check_props${MacroExpression.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro 'check props'.
         */
        fun parse(parser: LexemParser): MacroCheckProps? {
            parser.fromBuffer(parser.reader.currentPosition(), MacroCheckProps::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val value = PropertyStyleObjectBlockNode.parse(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.MacroCheckPropsWithoutPropertyStyleBlockAfterMacroName,
                    "A property-style block was expected after the macro name '$macroName'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an empty property-style block here '${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.endToken}'")
                }
            }

            val result = MacroCheckProps(parser, value)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
