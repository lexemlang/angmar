package org.lexem.angmar.parser.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.selective.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for else patterns of the selective statements.
 */
internal class ElsePatternSelectiveStmtNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    override fun toString() = StringBuilder().apply {
        append(elseKeyword)
    }.toString()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ElsePatternSelectiveStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val elseKeyword = ConditionalStmtNode.elseKeyword


        // METHODS ------------------------------------------------------------

        /**
         * Parses an else pattern of the selective statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ElsePatternSelectiveStmtNode? {
            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(elseKeyword)) {
                return null
            }

            val result = ElsePatternSelectiveStmtNode(parser, parent, parentSignal)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
