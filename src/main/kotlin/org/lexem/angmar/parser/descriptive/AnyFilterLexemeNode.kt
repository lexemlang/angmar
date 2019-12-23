package org.lexem.angmar.parser.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.expressions.macros.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for filter lexemes.
 */
internal class AnyFilterLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        AnyLexemeNode(parser, parent) {

    companion object {

        /**
         * Parses a lexemes.
         */
        fun parse(parser: LexemParser, parent: ParserNode): AnyFilterLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AnyFilterLexemeNode(parser, parent)

            var lexeme: ParserNode? =
                    CheckPropsMacroNode.parse(parser, result) ?: MacroBacktrackNode.parse(parser, result)

            if (lexeme != null) {
                result.lexeme = lexeme
                return parser.finalizeNode(result, initCursor)
            }

            // Data capturing
            result.dataCapturing = AnyLexemeNode.parseDataCapturing(parser, result)
            if (result.dataCapturing != null) {
                WhitespaceNoEOLNode.parse(parser)
            }

            // Lexeme
            lexeme = AdditionFilterLexemeNode.parse(parser, result) ?: FilterLexemeNode.parse(parser, result)
                    ?: QuantifiedGroupLexemeNode.parse(parser, result) ?: ExecutorLexemeNode.parse(parser, result)
                    ?: BlockStmtNode.parse(parser, result) ?: AccessLexemeNode.parse(parser, result)
                    ?: GroupLexemeNode.parse(parser, result)

            if (lexeme == null) {
                initCursor.restore()
                return null
            }

            result.lexeme = lexeme

            // Quantifier
            result.quantifier = QuantifierLexemeNode.parse(parser, result)

            return parser.finalizeNode(result, initCursor)
        }
    }
}
