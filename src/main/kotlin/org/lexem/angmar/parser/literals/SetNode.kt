package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for set literals
 */
class SetNode private constructor(parser: LexemParser, val list: ListNode) : ParserNode(parser) {

    override fun toString() = list.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("list", list)
    }

    companion object {
        const val macroName = "set${MacroExpression.macroSuffix}"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a set literal
         */
        fun parse(parser: LexemParser): SetNode? {
            parser.fromBuffer(parser.reader.currentPosition(), SetNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val list = ListNode.parse(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.SetWithoutStartToken,
                    "The open square bracket was expected '${ListNode.startToken}' after the macro name '$macroName'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding the open square bracket '${ListNode.startToken}' here")
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the macro name '$macroName'")
                }
            }

            val result = SetNode(parser, list)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
