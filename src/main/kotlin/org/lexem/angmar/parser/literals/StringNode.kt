package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for normal string literals.
 */
class StringNode private constructor(parser: LexemParser) : ParserNode(parser) {
    val texts = mutableListOf<String>()
    val escapes = mutableListOf<ParserNode>()

    val minAdditionalDelimiterRequired
        get() = texts.map { text ->
            endingTokenRegex.findAll(text).maxBy { it.value.length }?.value?.length ?: 0
        }.max() ?: 0

    override fun toString() = StringBuilder().apply {
        val delimiter = additionalDelimiter.repeat(minAdditionalDelimiterRequired)

        append(delimiter)
        append(startToken)
        for (i in 0 until texts.size - 1) {
            append(texts[i])
            append(escapes[i])
        }
        append(texts.last())
        append(endToken)
        append(delimiter)
    }.toString()


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("texts", texts)
        printer.addField("escapes", escapes)
    }

    companion object {
        const val additionalDelimiter = "$"
        const val startToken = "\""
        const val endToken = "\""
        private const val notAllowedChars = EscapeNode.startToken
        val endingTokenRegex = Regex("$startToken\\$additionalDelimiter*")

        // METHODS ------------------------------------------------------------

        /**
         * Parses a normal string literal.
         */
        fun parse(parser: LexemParser): StringNode? {
            parser.fromBuffer(parser.reader.currentPosition(), StringNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = StringNode(parser)

            var ending = ""

            while (parser.readText(additionalDelimiter)) {
                ending += additionalDelimiter
            }

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            ending = "$endToken$ending"

            result.texts.add(readStringSection(parser, ending) ?: "")

            while (!parser.readText(ending)) {
                val escape = Commons.parseAnyEscape(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.StringWithoutEndQuote,
                        "String literals require the end quote '$ending' to finish the literal.") {
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

                result.escapes.add(escape)
                result.texts.add(readStringSection(parser, ending) ?: "")
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Reads a valid string section.
         */
        private fun readStringSection(parser: LexemParser, ending: String): String? {
            if (parser.checkText(ending)) {
                return null
            }

            val result = StringBuilder()

            result.append(parser.readNegativeAnyChar(notAllowedChars) ?: return null)

            while (!parser.checkText(ending)) {
                result.append(parser.readNegativeAnyChar(notAllowedChars) ?: break)
            }

            return result.toString()
        }
    }
}
