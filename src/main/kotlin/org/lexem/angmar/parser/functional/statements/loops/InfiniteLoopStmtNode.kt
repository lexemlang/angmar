package org.lexem.angmar.parser.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for infinite loop statements.
 */
class InfiniteLoopStmtNode private constructor(parser: LexemParser, val thenBlock: ParserNode) : ParserNode(parser) {
    var index: IdentifierNode? = null
    var lastClauses: LoopClausesStmtNode? = null

    override fun toString() = StringBuilder().apply {
        append(keyword)
        append(' ')

        if (index != null) {
            append(index)
            append(' ')
        }

        append(thenBlock)
        if (lastClauses != null) {
            append('\n')
            append(lastClauses)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addOptionalField("index", index)
        printer.addField("thenBlock", thenBlock)
        printer.addOptionalField("lastClauses", lastClauses)
    }

    companion object {
        const val keyword = "repeat"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an infinite loop statement.
         */
        fun parse(parser: LexemParser): InfiniteLoopStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), InfiniteLoopStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            // index
            var index: IdentifierNode?
            let {
                val initIndexCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                index = IdentifierNode.parse(parser)
                if (index == null) {
                    initIndexCursor.restore()
                }
            }

            WhitespaceNode.parse(parser)

            val thenBlock = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.InfiniteLoopStatementWithoutBlock,
                    "A block was expected after the loop keyword '$keyword' to act as the code of the loop.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here")
                }
                addNote("WARNING", "An empty block will cause an infinite loop")
            }

            // last clauses
            var lastClauses: LoopClausesStmtNode?
            let {
                val initLastClausesCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                lastClauses = LoopClausesStmtNode.parse(parser)
                if (lastClauses == null) {
                    initLastClausesCursor.restore()
                }
            }

            val result = InfiniteLoopStmtNode(parser, thenBlock)
            result.index = index
            result.lastClauses = lastClauses

            return parser.finalizeNode(result, initCursor)
        }
    }
}
