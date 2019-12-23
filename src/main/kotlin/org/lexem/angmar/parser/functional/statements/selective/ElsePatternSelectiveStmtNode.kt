package org.lexem.angmar.parser.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for else patterns of the selective statements.
 */
internal class ElsePatternSelectiveStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {

    override fun toString() = StringBuilder().apply {
        append(elseKeyword)
    }.toString()

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            ConstantCompiled(parent, parentSignal, this, LxmLogic.True)

    companion object {
        const val elseKeyword = ConditionalStmtNode.elseKeyword
        
        // METHODS ------------------------------------------------------------

        /**
         * Parses an else pattern of the selective statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode): ElsePatternSelectiveStmtNode? {
            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(elseKeyword)) {
                return null
            }

            val result = ElsePatternSelectiveStmtNode(parser, parent)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
