package org.lexem.angmar.parser.functional.statements.loops

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.loops.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for infinite loop statements.
 */
internal class InfiniteLoopStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var thenBlock: ParserNode
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

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("index", index?.toTree())
        result.add("thenBlock", thenBlock.toTree())
        result.add("lastClauses", lastClauses?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            InfiniteLoopStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val keyword = "repeat"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an infinite loop statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): InfiniteLoopStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), InfiniteLoopStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = InfiniteLoopStmtNode(parser, parent, parentSignal)

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            // index
            let {
                val initIndexCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                result.index = IdentifierNode.parse(parser, result, InfiniteLoopStmtAnalyzer.signalEndIndex)
                if (result.index == null) {
                    initIndexCursor.restore()
                }
            }

            WhitespaceNode.parse(parser)

            result.thenBlock = BlockStmtNode.parse(parser, result, InfiniteLoopStmtAnalyzer.signalEndThenBlock)
                    ?: throw AngmarParserException(AngmarParserExceptionType.InfiniteLoopStatementWithoutBlock,
                            "A block was expected after the loop keyword '$keyword' to act as the code of the loop.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message =
                                    "Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here"
                        }
                        addNote("WARNING", "An empty block will cause an infinite loop")
                    }

            // last clauses
            let {
                val initLastClausesCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                result.lastClauses =
                        LoopClausesStmtNode.parse(parser, result, InfiniteLoopStmtAnalyzer.signalEndLastClause)
                if (result.lastClauses == null) {
                    initLastClausesCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
