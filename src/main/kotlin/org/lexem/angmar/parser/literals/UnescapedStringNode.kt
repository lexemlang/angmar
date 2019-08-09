package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*


/**
 * Parser for unescaped string literals.
 */
class UnescapedStringNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var text: String = ""

    val minAdditionalDelimiterRequired
        get() = endingTokenRegex.findAll(text).maxBy { it.value.length }?.value?.length ?: 0

    override fun toString() = StringBuilder().apply {
        val delimiter = additionalDelimiter.repeat(minAdditionalDelimiterRequired)

        append(macroName)
        append(delimiter)
        append(startToken)
        append(text)
        append(endToken)
        append(delimiter)
    }.toString()


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("text", text)
    }

    companion object {
        const val macroName = "lit!"
        const val startToken = StringNode.startToken
        const val endToken = StringNode.endToken
        const val additionalDelimiter = StringNode.additionalDelimiter
        val endingTokenRegex = StringNode.endingTokenRegex

        // METHODS ------------------------------------------------------------

        /**
         * Parses a normal string literal.
         */
        fun parse(parser: LexemParser): UnescapedStringNode? {
            parser.fromBuffer(parser.reader.currentPosition(), UnescapedStringNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = UnescapedStringNode(parser)

            if (!parser.readText(macroName)) {
                return null
            }

            var ending = ""

            while (parser.readText(additionalDelimiter)) {
                ending += additionalDelimiter
            }

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            ending = "$endToken$ending"

            val text = StringBuilder()

            while (!parser.readText(ending)) {
                if (parser.reader.isEnd()) {
                    throw AngmarParserException(AngmarParserExceptionType.UnescapedStringWithoutEndQuote,
                            "Unescaped String literals require the end quote '$ending' to finish the literal.") {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightCursorAt(parser.reader.currentPosition())
                            message("Try adding the end quote '$ending' here")
                        }
                    }
                }

                text.append(parser.reader.currentChar())
                parser.reader.advance()
            }

            result.text = text.toString()

            return parser.finalizeNode(result, initCursor)
        }
    }
}
