package org.lexem.angmar.nodes.commons

import es.jtp.kterm.*
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.Consts
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.ITextReaderCursor
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode

/**
 * Parser for multiline comments.
 */
class CommentMultilineNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var initTextCursor: ITextReaderCursor = ITextReaderCursor.Empty
    var endTextCursor: ITextReaderCursor = ITextReaderCursor.Empty
    var additionalTokens = ""

    val text by lazy {
        parser.substring(initTextCursor, endTextCursor)
    }

    override fun toString() = StringBuilder().apply {
        append(MultilineStartToken)
        append(additionalTokens)
        append(text)
        append(additionalTokens)
        append(MultilineEndToken)
    }.toString()


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("text", text)
        printer.addOptionalField("additionalTokens", additionalTokens)
    }

    companion object {
        private val nodeType = NodeType.CommentMultiline
        const val MultilineStartToken = "#+"
        const val MultilineEndToken = "+#"
        const val MultilineAdditionalToken = "+"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a multi-line comment.
         */
        fun parse(parser: LexemParser): CommentMultilineNode? {
            parser.fromBuffer<CommentMultilineNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = CommentMultilineNode(parser)

            if (!parser.readText(MultilineStartToken)) {
                return null
            }

            // Additional tokens.
            result.additionalTokens = StringBuilder().apply {
                while (parser.readText(MultilineAdditionalToken)) {
                    append(MultilineAdditionalToken)
                }
            }.toString()

            result.initTextCursor = parser.reader.saveCursor()
            result.endTextCursor = result.initTextCursor

            while (true) {
                if (parser.readText(result.additionalTokens)) {
                    if (parser.readText(MultilineEndToken)) {
                        break
                    }

                    result.endTextCursor.restore()
                }

                if (parser.reader.isEnd()) {
                    throw AngmarParserException(Logger.build(
                        "Multiline comments require the end token '${result.additionalTokens}$MultilineEndToken' but the end-of-file (EOF) was encountered."
                    ) {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(parser.reader.currentPosition())
                            message("Try adding the end token '${result.additionalTokens}$MultilineEndToken' here")
                        }
                    })
                }
                parser.reader.advance()
                result.endTextCursor = parser.reader.saveCursor()
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}