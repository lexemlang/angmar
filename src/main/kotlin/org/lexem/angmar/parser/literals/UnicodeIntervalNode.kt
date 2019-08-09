package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for unicode interval literals.
 */
class UnicodeIntervalNode private constructor(parser: LexemParser, val node: UnicodeIntervalAbbrNode) :
        ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(node)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("node", node)
    }

    companion object {
        const val macroName = "uitv${MacroExpression.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an unicode interval literal.
         */
        fun parse(parser: LexemParser): UnicodeIntervalNode? {
            parser.fromBuffer(parser.reader.currentPosition(), UnicodeIntervalNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val node = UnicodeIntervalAbbrNode.parse(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.UnicodeIntervalWithoutStartToken,
                    "The start square bracket was expected '${UnicodeIntervalAbbrNode.startToken}'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding the start and end square brackets '${UnicodeIntervalAbbrNode.startToken}${UnicodeIntervalAbbrNode.endToken}' here")
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the '$macroName' macro")
                }
            }

            val result = UnicodeIntervalNode(parser, node)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
