package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for public macro statement.
 */
class PublicMacroStmtNode private constructor(parser: LexemParser, val element: ParserNode) : ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(' ')
        append(element)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("element", element)
    }

    companion object {
        const val macroName = "pub${MacroExpression.macroSuffix}"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a public macro statement.
         */
        fun parse(parser: LexemParser): PublicMacroStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PublicMacroStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            WhitespaceNode.parse(parser)

            val element = StatementCommons.parseAnyPublicMacroStatement(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.PublicMacroStatementWithoutValidStatement,
                    "A valid statement was expected after the '$macroName' macro.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), initCursor.position() + macroName.length - 1)
                    message("Try removing the '$macroName' macro")
                }
                addNote(Consts.Logger.hintTitle,
                        "The valid statements are variable, function, expression or filter declarations.")
            }

            val result = PublicMacroStmtNode(parser, element)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
