package org.lexem.angmar.parser.descriptive

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.*


/**
 * Parser for lexeme pattern.
 */
internal class LexemePatternNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var type = PatternType.Alternative
    var quantifier: ExplicitQuantifierLexemeNode? = null
    var unionName: IdentifierNode? = null
    var patternContent: LexemePatternContentNode? = null

    override fun toString() = StringBuilder().apply {
        append(patternToken)
        append(type.token)

        if (quantifier != null) {
            append(quantifier)
        } else if (type == PatternType.Quantified) {
            append(quantifierSlaveToken)
        }

        if (unionName != null) {
            append(unionName)
            append(unionNameRelationalToken)
        }

        if (patternContent != null) {
            append(" ")
            append(patternContent)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("type", type.toString())
        result.add("quantifier", quantifier?.toTree())
        result.add("unionName", unionName?.toTree())
        result.add("patternContent", patternContent?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            LexemePatternCompiled.compile(parent, parentSignal, this)

    companion object {
        const val patternToken = "|"
        const val staticTypeToken = ">"
        const val optionalTypeToken = "?"
        const val negativeTypeToken = "!"
        const val additiveTypeToken = "+"
        const val selectiveTypeToken = "*"
        const val quantifierSlaveToken =
                "${ExplicitQuantifierLexemeNode.startToken}${ExplicitQuantifierLexemeNode.endToken}"
        const val unionNameRelationalToken = staticTypeToken

        val singlePatterns = listOf(PatternType.Static, PatternType.Optional, PatternType.Negative)
        val unionPatterns =
                listOf(PatternType.Alternative, PatternType.Additive, PatternType.Selective, PatternType.Quantified)

        // METHODS ------------------------------------------------------------

        /**
         * Parses a lexeme pattern.
         */
        fun parse(parser: LexemParser, parent: ParserNode): LexemePatternNode? {
            val initCursor = parser.reader.saveCursor()
            val result = LexemePatternNode(parser, parent)

            if (!parser.readText(patternToken)) {
                return null
            }

            when {
                parser.readText(staticTypeToken) -> result.type = PatternType.Static
                parser.readText(optionalTypeToken) -> result.type = PatternType.Optional
                parser.readText(negativeTypeToken) -> result.type = PatternType.Negative
                else -> {
                    when {
                        parser.readText(additiveTypeToken) -> result.type = PatternType.Additive
                        parser.readText(selectiveTypeToken) -> result.type = PatternType.Selective
                        parser.readText(quantifierSlaveToken) -> result.type = PatternType.Quantified
                        else -> {
                            result.quantifier = ExplicitQuantifierLexemeNode.parse(parser, result)

                            if (result.quantifier != null) {
                                result.type = PatternType.Quantified
                            }
                        }
                    }

                    // Union name
                    let {
                        val preUnionNameCursor = parser.reader.saveCursor()

                        WhitespaceNoEOLNode.parse(parser)

                        val unionName = IdentifierNode.parse(parser, result) ?: return@let

                        WhitespaceNoEOLNode.parse(parser)

                        if (!parser.readText(unionNameRelationalToken)) {
                            preUnionNameCursor.restore()
                            return@let
                        }

                        result.unionName = unionName
                    }
                }
            }

            // Pattern content
            let {
                val prePatternCursor = parser.reader.saveCursor()

                WhitespaceNoEOLNode.parse(parser)

                val patternContent = LexemePatternContentNode.parse(parser, result)

                if (patternContent == null) {
                    prePatternCursor.restore()
                    return@let
                }

                result.patternContent = patternContent
            }

            return parser.finalizeNode(result, initCursor)
        }

        // ENUMS --------------------------------------------------------------

        enum class PatternType(val token: String) {
            Static(staticTypeToken),
            Optional(optionalTypeToken),
            Negative(negativeTypeToken),
            Additive(additiveTypeToken),
            Selective(selectiveTypeToken),
            Quantified(""),
            Alternative("")
        }
    }
}
