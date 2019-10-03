package org.lexem.angmar.parser.commons

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * Parser for multiline comments.
 */
internal object CommentMultilineNode {
    private const val MultilineBoundToken = "#"
    const val MultilineStartToken = "$MultilineBoundToken-"
    const val MultilineEndToken = "-$MultilineBoundToken"
    const val MultilineAdditionalToken = "-"

    // METHODS ------------------------------------------------------------

    /**
     * Parses a multi-line comment.
     */
    fun parse(parser: LexemParser): Boolean {
        val initCursor = parser.reader.saveCursor()

        if (!parser.readText(MultilineStartToken)) {
            return false
        }

        // Additional tokens.
        val additionalTokens = StringBuilder().apply {
            while (parser.readText(MultilineAdditionalToken)) {
                append(MultilineAdditionalToken)
            }
        }.toString()

        // Premature ending.
        if (parser.readText(MultilineBoundToken)) {
            return true
        }

        var endTextCursor = parser.reader.saveCursor()

        while (true) {
            if (parser.readText(additionalTokens)) {
                if (parser.readText(MultilineEndToken)) {
                    break
                }

                endTextCursor.restore()
            }

            if (parser.reader.isEnd()) {
                throw AngmarParserException(AngmarParserExceptionType.MultilineCommentWithoutEndToken,
                        "Multiline comments require the end token '$additionalTokens$MultilineEndToken' but the end-of-file (EOF) was encountered.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the end token '$additionalTokens$MultilineEndToken' here"
                    }
                }
            }
            parser.reader.advance()
            endTextCursor = parser.reader.saveCursor()
        }

        return true
    }
}
