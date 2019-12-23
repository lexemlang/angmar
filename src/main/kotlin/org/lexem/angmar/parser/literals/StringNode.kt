package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for normal string literals.
 */
internal class StringNode private constructor(parser: LexemParser, parent: ParserNode) : ParserNode(parser, parent) {
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

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("texts", SerializationUtils.stringListToTest(texts))
        result.add("escapes", SerializationUtils.listToTest(escapes))

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) = StringCompiled.compile(parent, parentSignal, this)

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
        fun parse(parser: LexemParser, parent: ParserNode): StringNode? {
            val initCursor = parser.reader.saveCursor()
            val result = StringNode(parser, parent)

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
                val escape = Commons.parseAnyEscape(parser, result) ?: throw AngmarParserException(
                        AngmarParserExceptionType.StringWithoutEndQuote,
                        "String literals require the end quote '$ending' to finish the literal.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the end quote '$ending' here"
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
