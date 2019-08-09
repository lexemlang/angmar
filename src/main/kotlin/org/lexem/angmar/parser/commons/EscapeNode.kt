package org.lexem.angmar.parser.commons

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*

/**
 * Parser for escaped characters.
 */
class EscapeNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var value: String? = null

    override fun toString() = "$startToken$value"

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("value", value)
    }

    companion object {
        internal const val startToken = "\\"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an escape.
         */
        fun parse(parser: LexemParser): EscapeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), EscapeNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = EscapeNode(parser)

            if (!parser.readText(startToken)) {
                return null
            }

            result.value = WhitespaceNode.readLineBreak(parser).takeIf { it != null && it != "" }
                    ?: parser.reader.currentChar()?.toString() ?: let {
                        throw AngmarParserException(AngmarParserExceptionType.EscapeWithoutCharacter,
                                "Escapes require a character after the escape token '$startToken' but the end-of-file (EOF) was encountered.") {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightCursorAt(initCursor.position() + 1)
                                message("Try adding a character here")
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightSection(initCursor.position())
                                message("Try removing the escape token '$startToken' here")
                            }
                        }
                    }

            parser.reader.advance()
            return parser.finalizeNode(result, initCursor)
        }
    }
}
