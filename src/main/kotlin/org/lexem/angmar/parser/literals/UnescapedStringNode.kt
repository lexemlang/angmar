package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*


/**
 * Parser for unescaped string literals.
 */
internal class UnescapedStringNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
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

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("text", text)

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            ConstantCompiled(parent, parentSignal, this, LxmString.from(text))

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
        fun parse(parser: LexemParser, parent: ParserNode): UnescapedStringNode? {
            val initCursor = parser.reader.saveCursor()
            val result = UnescapedStringNode(parser, parent)

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
                }

                text.append(parser.reader.currentChar())
                parser.reader.advance()
            }

            result.text = text.toString()

            return parser.finalizeNode(result, initCursor)
        }
    }
}
