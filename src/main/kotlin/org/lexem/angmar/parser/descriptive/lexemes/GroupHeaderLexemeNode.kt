package org.lexem.angmar.parser.descriptive.lexemes

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for group header lexemes.
 */
internal class GroupHeaderLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
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

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            GroupHeaderLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses a group header lexeme.
         */
        fun parse(parser: LexemParser, parent: ParserNode): GroupHeaderLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = GroupHeaderLexemeNode(parser, parent)
            result.isFilterCode = parser.isFilterCode

            result.quantifier = QuantifierLexemeNode.parse(parser, result)

            // Identifier
            let {
                val preIdentifierCursor = parser.reader.saveCursor()

                if (result.quantifier != null) {
                    WhitespaceNode.parse(parser)
                }

                result.identifier = IdentifierNode.parse(parser, result)

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

                result.propertyBlock = PropertyStyleObjectBlockNode.parse(parser, result)

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
