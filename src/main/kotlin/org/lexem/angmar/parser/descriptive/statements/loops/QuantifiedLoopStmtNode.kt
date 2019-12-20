package org.lexem.angmar.parser.descriptive.statements.loops

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.loops.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.loops.*


/**
 * Parser for quantified loop statements.
 */
internal class QuantifiedLoopStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var quantifier: QuantifierLexemeNode
    lateinit var thenBlock: ParserNode
    var index: IdentifierNode? = null
    var lastClauses: LoopClausesStmtNode? = null

    override fun toString() = StringBuilder().apply {
        if (index != null) {
            append(InfiniteLoopStmtNode.keyword)
            append(' ')
            append(index)
            append(' ')
        }

        append(keyword)
        append(' ')
        append(quantifier)
        append(' ')
        append(thenBlock)
        if (lastClauses != null) {
            append('\n')
            append(lastClauses)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("index", index?.toTree())
        result.add("quantifier", quantifier.toTree())
        result.add("thenBlock", thenBlock.toTree())
        result.add("lastClauses", lastClauses?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            QuantifiedLoopStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val keyword = "for"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a quantified loop statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): QuantifiedLoopStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = QuantifiedLoopStmtNode(parser, parent, parentSignal)

            // index
            let {
                if (!Commons.parseKeyword(parser, InfiniteLoopStmtNode.keyword)) {
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.index = IdentifierNode.parse(parser, result, QuantifiedLoopStmtAnalyzer.signalEndIndex)
                if (result.index == null) {
                    initCursor.restore()
                    return null
                }

                WhitespaceNode.parse(parser)
            }

            if (!Commons.parseKeyword(parser, keyword)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            result.quantifier =
                    QuantifierLexemeNode.parse(parser, result, QuantifiedLoopStmtAnalyzer.signalEndQuantifier)
                            ?: throw AngmarParserException(
                                    AngmarParserExceptionType.QuantifiedLoopStatementWithoutQuantifier,
                                    "An expression was expected after the loop keyword '$keyword' to act as the condition.") {
                                val fullText = parser.reader.readAllText()
                                addSourceCode(fullText, parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightCursorAt(parser.reader.currentPosition())
                                    message = "Try adding a quantifier here"
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    message = "Try removing the loop keyword '$keyword'"
                                }
                            }

            WhitespaceNode.parse(parser)

            result.thenBlock = BlockStmtNode.parse(parser, result, QuantifiedLoopStmtAnalyzer.signalEndThenBlock)
                    ?: throw AngmarParserException(AngmarParserExceptionType.QuantifiedLoopStatementWithoutBlock,
                            "A block was expected to be the body of the quantified loop.") {
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
                    }

            // last clauses
            let {
                val initLastClausesCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val lastClauses =
                        LoopClausesStmtNode.parse(parser, result, QuantifiedLoopStmtAnalyzer.signalEndLastClause)
                if (lastClauses == null) {
                    initLastClausesCursor.restore()
                }

                result.lastClauses = lastClauses
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
