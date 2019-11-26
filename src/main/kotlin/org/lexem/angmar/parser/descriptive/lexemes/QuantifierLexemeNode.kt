package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.parser.*


/**
 * Parser for quantifier lexemes.
 */
internal class QuantifierLexemeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var abbreviation: Char? = null
    var quantifier: ExplicitQuantifierLexemeNode? = null
    var modifier: Char? = null

    override fun toString() = StringBuilder().apply {
        if (abbreviation != null) {
            append(abbreviation!!)
        } else {
            append(quantifier)
        }

        if (modifier != null) {
            append(modifier!!)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        if (abbreviation != null) {
            result.addProperty("abbreviation", abbreviation!!)
        } else {
            result.add("quantifier", quantifier!!.toTree())
        }

        result.addProperty("modifier", modifier)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            QuantifierLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val lazyAbbreviation = "?"
        const val atomicGreedyAbbreviations = "+"
        const val atomicLazyAbbreviations = "*"
        const val abbreviations = "$lazyAbbreviation$atomicGreedyAbbreviations$atomicLazyAbbreviations"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a quantifier lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): QuantifierLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), QuantifierLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = QuantifierLexemeNode(parser, parent, parentSignal)

            val quantifier =
                    ExplicitQuantifierLexemeNode.parse(parser, result, QuantifierLexemAnalyzer.endQuantifierSignal)

            if (quantifier != null) {
                result.quantifier = quantifier
            } else {
                result.abbreviation = parser.readAnyChar(abbreviations)
                if (result.abbreviation == null) {
                    return null
                }
            }

            result.modifier = parser.readAnyChar(abbreviations)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
