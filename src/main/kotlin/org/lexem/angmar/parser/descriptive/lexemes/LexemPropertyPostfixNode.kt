package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*


/**
 * Parser for property postfix of lexemes.
 */
internal class LexemPropertyPostfixNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    val positiveElements = mutableListOf<String>()
    val negativeElements = mutableListOf<String>()
    val reversedElements = mutableListOf<String>()

    override fun toString() = StringBuilder().apply {
        if (positiveElements.isNotEmpty()) {
            append(positiveElements.joinToString(" "))
        }

        if (negativeElements.isNotEmpty()) {
            append(negativeToken)
            append(' ')
            append(negativeElements.joinToString(" "))
        }

        if (reversedElements.isNotEmpty()) {
            append(reversedToken)
            append(' ')
            append(reversedElements.joinToString(" "))
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("positiveElements", SerializationUtils.stringListToTest(positiveElements))
        result.add("negativeElements", SerializationUtils.stringListToTest(negativeElements))
        result.add("reversedElements", SerializationUtils.stringListToTest(reversedElements))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = throw AngmarUnreachableException()

    companion object {
        const val negativeToken = AdditiveExpressionNode.subtractionOperator
        const val reversedToken = PrefixOperatorNode.notOperator
        const val reversedProperty = "r"
        const val insensibleProperty = "i"
        const val properties = "$insensibleProperty$reversedProperty"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a property-style object blocks
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): LexemPropertyPostfixNode? {
            val initCursor = parser.reader.saveCursor()
            val result = LexemPropertyPostfixNode(parser, parent, parentSignal)

            var hasParsedAnything = false

            while (true) {
                val element = parser.readAnyChar(properties) ?: break
                result.positiveElements.add(element.toString())
                hasParsedAnything = true
            }

            if (parser.readText(negativeToken)) {
                while (true) {
                    val element = parser.readAnyChar(properties) ?: break
                    result.negativeElements.add(element.toString())
                }

                if (result.negativeElements.isEmpty()) {
                    throw AngmarParserException(AngmarParserExceptionType.InlinePropertyPostfixWithoutProperty,
                            "The inline property postfix requires one of the following properties after the negative token '$negativeToken': ${properties.asSequence().joinToString(
                                    ",")}") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(parser.reader.currentPosition() - 1)
                            message = "Try removing the negative token '$negativeToken'"
                        }
                    }
                }

                hasParsedAnything = true
            }

            if (parser.readText(reversedToken)) {
                while (true) {
                    val element = parser.readAnyChar(properties) ?: break
                    result.reversedElements.add(element.toString())
                }

                if (result.reversedElements.isEmpty()) {
                    throw AngmarParserException(AngmarParserExceptionType.InlinePropertyPostfixWithoutProperty,
                            "The inline property postfix requires one of the following properties after the reversed token '$reversedToken': ${properties.asSequence().joinToString(
                                    ",")}") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(parser.reader.currentPosition() - 1)
                            message = "Try removing the reversed token '$reversedToken'"
                        }
                    }
                }

                hasParsedAnything = true
            }

            if (!hasParsedAnything) {
                initCursor.restore()
                return null
            }

            if (Commons.checkIdentifier(parser)) {
                throw AngmarParserException(AngmarParserExceptionType.InlinePropertyPostfixWithBadEnd,
                        "The inline property postfix cannot end with another textual character") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding a whitespace here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
