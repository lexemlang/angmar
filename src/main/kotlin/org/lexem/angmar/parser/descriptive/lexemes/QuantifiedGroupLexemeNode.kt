package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for quantified group lexemes.
 */
internal class QuantifiedGroupLexemeNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    var mainModifier: QuantifiedGroupModifierNode? = null
    val patterns = mutableListOf<LexemePatternContentNode>()
    val modifiers = mutableListOf<QuantifierLexemeNode?>()

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        append(startToken)

        if (mainModifier != null) {
            append(mainModifier)
        }

        val patternsText = patterns.mapIndexed { i, pat ->
            if (modifiers[i] != null) {
                "$pat $patternToken${modifiers[i]}"
            } else {
                "$pat $patternToken"
            }
        }
        append(patternsText.joinToString(" "))

        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.add("mainModifier", mainModifier?.toTree())
        result.add("patterns", SerializationUtils.listToTest(patterns))
        result.add("modifiers", SerializationUtils.nullableListToTest(modifiers))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            QuantifiedGroupLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val startToken = "@("
        const val endToken = ")"
        const val patternToken = LexemePatternNode.patternToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a quantified group lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): QuantifiedGroupLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), QuantifiedGroupLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = QuantifiedGroupLexemeNode(parser, parent, parentSignal)

            result.isNegated = parser.readText(notOperator)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            result.mainModifier = QuantifiedGroupModifierNode.parse(parser, result,
                    QuantifiedGroupLexemAnalyzer.signalEndMainModifier)

            WhitespaceNode.parse(parser)

            // Patterns and modifiers
            while (true) {
                val pattern = LexemePatternContentNode.parse(parser, result,
                        result.patterns.size + QuantifiedGroupLexemAnalyzer.signalEndFirstPattern) ?: break

                WhitespaceNode.parse(parser)

                if (!parser.readText(patternToken)) {
                    val patternCursor = parser.reader.saveCursor()

                    // To show the end token in the message if it exists.
                    parser.readText(endToken)

                    throw AngmarParserException(AngmarParserExceptionType.QuantifiedGroupPatternWithoutLexemes,
                            "Quantified group lexemes require the pattern token '$patternToken' at the end of each pattern.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(patternCursor.position())
                            message = "Try adding the pattern token '$patternToken' here"
                        }
                    }
                }

                val modifier = QuantifierLexemeNode.parse(parser, result,
                        result.modifiers.size + QuantifiedGroupLexemAnalyzer.signalEndFirstPattern)

                result.patterns.add(pattern)
                result.modifiers.add(modifier)

                WhitespaceNode.parse(parser)
            }

            if (result.patterns.isEmpty()) {
                val patternCursor = parser.reader.saveCursor()

                // To show the end token in the message if it exists.
                parser.readText(endToken)

                throw AngmarParserException(AngmarParserExceptionType.QuantifiedGroupWithoutEndToken,
                        "Quantified group lexemes require at least one lexeme pattern.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(patternCursor.position())
                        message = "Try adding a lexeme pattern here"
                    }
                }
            }

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.QuantifiedGroupWithoutEndToken,
                        "Quantified group lexemes require the close parenthesis '$endToken'.") {
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

            // Add the offset to the modifiers.
            val offset = result.patterns.size
            for (i in result.modifiers) {
                if (i != null) {
                    i.parentSignal += offset
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
