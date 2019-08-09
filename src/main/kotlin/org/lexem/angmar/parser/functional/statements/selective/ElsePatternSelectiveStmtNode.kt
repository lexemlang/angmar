package org.lexem.angmar.parser.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for else patterns of the selective statements.
 */
class ElsePatternSelectiveStmtNode private constructor(parser: LexemParser) : ParserNode(parser) {
    override fun toString() = StringBuilder().apply {
        append(elseKeyword)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
    }

    companion object {
        const val elseKeyword = ConditionalStmtNode.elseKeyword


        // METHODS ------------------------------------------------------------

        /**
         * Parses an else pattern of the selective statements.
         */
        fun parse(parser: LexemParser): ElsePatternSelectiveStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ElsePatternSelectiveStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(elseKeyword)) {
                return null
            }

            val result = ElsePatternSelectiveStmtNode(parser)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
