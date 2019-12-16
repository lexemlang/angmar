package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for group header lexemes.
 */
internal class GroupHeaderLexemeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var quantifier: QuantifierLexemeNode? = null
    var identifier: IdentifierNode? = null
    var propertyBlock: PropertyStyleObjectBlockNode? = null
    var isFilterCode = false

    override fun toString() = StringBuilder().apply {
        if (quantifier != null) {
            append(quantifier)
        }

        if (identifier != null) {
            append(identifier)
        }

        if (propertyBlock != null) {
            append(propertyBlock)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("quantifier", quantifier?.toTree())
        result.add("identifier", identifier?.toTree())
        result.add("propertyBlock", propertyBlock?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            GroupHeaderLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        /**
         * Parses a group header lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): GroupHeaderLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), GroupHeaderLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = GroupHeaderLexemeNode(parser, parent, parentSignal)
            result.isFilterCode = parser.isFilterCode

            result.quantifier = QuantifierLexemeNode.parse(parser, result, GroupHeaderLexemAnalyzer.signalEndQuantifier)

            // Identifier
            let {
                val preIdentifierCursor = parser.reader.saveCursor()

                if (result.quantifier != null) {
                    WhitespaceNode.parse(parser)
                }

                result.identifier = IdentifierNode.parse(parser, result, GroupHeaderLexemAnalyzer.signalEndIdentifier)

                if (result.identifier == null) {
                    preIdentifierCursor.restore()
                }
            }

            // PropertyBlock
            let {
                val prePropertyBlockCursor = parser.reader.saveCursor()

                if (result.quantifier != null || result.identifier != null) {
                    WhitespaceNode.parse(parser)
                }

                result.propertyBlock = PropertyStyleObjectBlockNode.parse(parser, result,
                        GroupHeaderLexemAnalyzer.signalEndPropertyBlock)

                if (result.propertyBlock == null) {
                    prePropertyBlockCursor.restore()
                }
            }

            if (result.quantifier == null && result.identifier == null && result.propertyBlock == null) {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
