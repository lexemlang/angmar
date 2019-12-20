package org.lexem.angmar.parser.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.expressions.macros.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for filter lexemes.
 */
internal class AnyFilterLexemeNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        AnyLexemeNode(parser, parent, parentSignal) {

    companion object {

        /**
         * Parses a lexemes.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): AnyFilterLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AnyFilterLexemeNode(parser, parent, parentSignal)

            var lexeme: ParserNode? = MacroCheckPropsNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                    ?: MacroBacktrackNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)

            if (lexeme != null) {
                result.lexeme = lexeme
                return parser.finalizeNode(result, initCursor)
            }

            // Data capturing
            result.dataCapturing =
                    AnyLexemeNode.parseDataCapturing(parser, result, AnyLexemeAnalyzer.signalEndDataCapturing)
            if (result.dataCapturing != null) {
                WhitespaceNoEOLNode.parse(parser)
            }

            // Lexeme
            lexeme = AdditionFilterLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
                    ?: FilterLexemeNode.parse(parser, result, AnyLexemeAnalyzer.signalEndLexem)
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
    }
}
