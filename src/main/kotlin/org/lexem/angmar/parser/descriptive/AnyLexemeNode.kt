package org.lexem.angmar.parser.descriptive

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.expressions.macros.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for lexemes.
 */
internal open class AnyLexemeNode protected constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var dataCapturing: DataCapturingAccessLexemeNode? = null
    lateinit var lexeme: ParserNode
    var quantifier: QuantifierLexemeNode? = null

    override fun toString() = StringBuilder().apply {
        if (dataCapturing != null) {
            append(dataCapturing)
            append(" = ")
        }

        append(lexeme)

        if (quantifier != null) {
            append(quantifier)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("dataCapturing", dataCapturing?.toTree())
        result.add("lexeme", lexeme.toTree())
        result.add("quantifier", quantifier?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            AnyLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        const val dataCapturingRelationalToken = AssignOperatorNode.assignOperator
        const val aliasToken = ":"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a lexemes.
         */
        fun parse(parser: LexemParser, parent: ParserNode): AnyLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AnyLexemeNode(parser, parent)

            var lexeme: ParserNode? =
                    CheckPropsMacroNode.parse(parser, result) ?: MacroBacktrackNode.parse(parser, result)
                    ?: SetPropsMacroStmtNode.parse(parser, result) ?: LexemeCommons.parseAnyAnchorLexeme(parser, result)

            if (lexeme != null) {
                result.lexeme = lexeme
                return parser.finalizeNode(result, initCursor)
            }

            // Data capturing
            result.dataCapturing = parseDataCapturing(parser, result)
            if (result.dataCapturing != null) {
                WhitespaceNoEOLNode.parse(parser)
            }

            // Lexeme
            lexeme = TextLexemeNode.parse(parser, result) ?: BinarySequenceLexemeNode.parse(parser, result)
                    ?: BlockLexemeNode.parse(parser, result) ?: QuantifiedGroupLexemeNode.parse(parser, result)
                    ?: ExecutorLexemeNode.parse(parser, result) ?: BlockStmtNode.parse(parser, result)
                    ?: AccessLexemeNode.parse(parser, result) ?: GroupLexemeNode.parse(parser, result)

            if (lexeme == null) {
                initCursor.restore()
                return null
            }

            result.lexeme = lexeme

            // Quantifier
            result.quantifier = QuantifierLexemeNode.parse(parser, result)

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a data capturing pattern.
         */
        fun parseDataCapturing(parser: LexemParser, parent: ParserNode): DataCapturingAccessLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val dataCapturing = DataCapturingAccessLexemeNode.parse(parser, parent) ?: return null

            WhitespaceNoEOLNode.parse(parser)

            if (!parser.readText(dataCapturingRelationalToken)) {
                initCursor.restore()
                return null
            }

            return dataCapturing
        }
    }
}
