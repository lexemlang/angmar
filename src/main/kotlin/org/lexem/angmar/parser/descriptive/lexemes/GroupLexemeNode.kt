package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for group lexemes.
 */
internal class GroupLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var isNegated = false
    var isFilterCode = false
    var header: GroupHeaderLexemeNode? = null
    val patterns = mutableListOf<LexemePatternContentNode>()

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(startToken)

        if (header != null) {
            append(header)
            append(headerRelationalToken)
        }

        append(patterns.joinToString(patternToken))

        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.addProperty("isFilterCode", isFilterCode)
        result.add("header", header?.toTree())
        result.add("patterns", SerializationUtils.listToTest(patterns))

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            GroupLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val startToken = "("
        const val endToken = ")"
        const val patternToken = LexemePatternNode.patternToken
        const val headerRelationalToken = ":"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a group lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode): GroupLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = GroupLexemeNode(parser, parent)

            result.isNegated = parser.readText(notOperator)
            result.isFilterCode = parser.isFilterCode

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            // Header
            let {
                val preHeaderCursor = parser.reader.saveCursor()

                val header = GroupHeaderLexemeNode.parse(parser, result) ?: return@let

                WhitespaceNode.parse(parser)

                if (!parser.readText(headerRelationalToken)) {
                    preHeaderCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.header = header
            }

            // Patterns
            var first = true
            while (true) {
                val preIterationCursor = parser.reader.saveCursor()

                if (!first) {
                    if (!parser.readText(patternToken)) {
                        preIterationCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)
                }

                val pattern = LexemePatternContentNode.parse(parser, result) ?: if (first) {
                    break
                } else {
                    val patternCursor = parser.reader.saveCursor()

                    // To show the end token in the message if it exists.
                    WhitespaceNode.parse(parser)
                    parser.readText(endToken)

                    throw AngmarParserException(AngmarParserExceptionType.GroupWithoutLexemeAfterPatternToken,
                            "Group lexemes require at least one lexeme after the pattern token '$patternToken'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(patternCursor.position() - 1)
                            message = "Try removing the pattern token '$patternToken'"
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(patternCursor.position())
                            message = "Try adding a lexeme here"
                        }
                    }
                }

                WhitespaceNode.parse(parser)

                result.patterns.add(pattern)
                first = false
            }

            if (result.patterns.isEmpty()) {
                val patternCursor = parser.reader.saveCursor()

                // To show the end token in the message if it exists.
                WhitespaceNode.parse(parser)
                parser.readText(endToken)

                throw AngmarParserException(AngmarParserExceptionType.GroupWithoutPatterns,
                        "Group lexemes require at least one pattern.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(patternCursor.position())
                        message = "Try adding a lexeme here"
                    }
                }
            }

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.GroupWithoutEndToken,
                        "Group lexemes require the close parenthesis '$endToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close parenthesis '$endToken' here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
