package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for set properties macro statement.
 */
class SetPropsMacroStmtNode private constructor(parser: LexemParser, val properties: ParserNode) : ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(properties)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("properties", properties)
    }

    companion object {
        const val macroName = "set_props${MacroExpression.macroSuffix}"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a set properties macro statement.
         */
        fun parse(parser: LexemParser): SetPropsMacroStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), SetPropsMacroStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val properties = PropertyStyleObjectBlockNode.parse(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.SetPropsMacroStatementWithoutPropertyStyleObject,
                    "A property-style object was expected after the '$macroName' macro.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an empty property-stye object here '${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.endToken}'")
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), initCursor.position() + macroName.length - 1)
                    message("Try removing the '$macroName' macro")
                }
            }

            val result = SetPropsMacroStmtNode(parser, properties)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
