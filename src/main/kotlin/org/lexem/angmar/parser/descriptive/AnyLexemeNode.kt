package org.lexem.angmar.parser.descriptive

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
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
internal open class AnyLexemeNode protected constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
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

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = AnyLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val dataCapturingRelationalToken = AssignOperatorNode.assignOperator
        const val aliasToken = ":"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a lexemes.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): AnyLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AnyLexemeNode(parser, parent, parentSignal)

            var lexeme: ParserNode? = MacroCheckPropsNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                    ?: MacroBacktrackNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                    ?: SetPropsMacroStmtNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                    ?: LexemeCommons.parseAnyAnchorLexeme(parser, result, AnyLexemeAnalyzer.signalEndLexem)

            if (lexeme != null) {
                result.lexeme = lexeme
                return parser.finalizeNode(result, initCursor)
            }

            // Data capturing
            result.dataCapturing = parseDataCapturing(parser, result, AnyLexemeAnalyzer.signalEndDataCapturing)
            if (result.dataCapturing != null) {
                WhitespaceNoEOLNode.parse(parser)
            }

            // Lexeme
            lexeme = TextLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                    ?: BinarySequenceLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                            ?: BlockLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                            ?: QuantifiedGroupLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                            ?: ExecutorLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                            ?: BlockStmtNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                            ?: AccessLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                            ?: GroupLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)

            if (lexeme == null) {
                initCursor.restore()
                return null
            }

            result.lexeme = lexeme

            // Quantifier
            result.quantifier = QuantifierLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndQuantifier)

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a data capturing pattern.
         */
        fun parseDataCapturing(parser: LexemParser, parent: ParserNode,
                parentSignal: Int): DataCapturingAccessLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val dataCapturing = DataCapturingAccessLexemeNode.parse(parser, parent, parentSignal) ?: return null

            WhitespaceNoEOLNode.parse(parser)

            if (!parser.readText(dataCapturingRelationalToken)) {
                initCursor.restore()
                return null
            }

            return dataCapturing
        }
    }
}
