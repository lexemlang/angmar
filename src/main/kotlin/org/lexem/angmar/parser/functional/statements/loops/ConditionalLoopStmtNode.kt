package org.lexem.angmar.parser.functional.statements.loops

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.loops.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for conditional loop statements.
 */
internal class ConditionalLoopStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var condition: ParserNode
    lateinit var thenBlock: ParserNode
    var isUntil = false
    var index: IdentifierNode? = null
    var lastClauses: LoopClausesStmtNode? = null

    override fun toString() = StringBuilder().apply {
        if (index != null) {
            append(InfiniteLoopStmtNode.keyword)
            append(' ')
            append(index)
            append(' ')
        }

        append(if (isUntil) {
            untilKeyword
        } else {
            whileKeyword
        })

        append(' ')
        append(condition)
        append(' ')
        append(thenBlock)
        if (lastClauses != null) {
            append('\n')
            append(lastClauses)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isUntil", isUntil)
        result.add("index", index?.toTree())
        result.add("condition", condition.toTree())
        result.add("thenBlock", thenBlock.toTree())
        result.add("lastClauses", lastClauses?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ConditionalLoopStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val whileKeyword = "while"
        const val untilKeyword = "until"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a conditional loop statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ConditionalLoopStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ConditionalLoopStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ConditionalLoopStmtNode(parser, parent, parentSignal)

            // index
            let {
                if (!Commons.parseKeyword(parser, InfiniteLoopStmtNode.keyword)) {
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.index = IdentifierNode.parse(parser, result, ConditionalLoopStmtAnalyzer.signalEndIndex)
                if (result.index == null) {
                    initCursor.restore()
                    return null
                }

                WhitespaceNode.parse(parser)
            }

            var conditionalKeyword = whileKeyword
            result.isUntil = when {
                Commons.parseKeyword(parser, whileKeyword) -> false
                Commons.parseKeyword(parser, untilKeyword) -> {
                    conditionalKeyword = untilKeyword
                    true
                }
                else -> {
                    initCursor.restore()
                    return null
                }
            }

            WhitespaceNode.parse(parser)

            result.condition =
                    ExpressionsCommons.parseExpression(parser, result, ConditionalLoopStmtAnalyzer.signalEndCondition)
                            ?: throw AngmarParserException(
                                    AngmarParserExceptionType.ConditionalLoopStatementWithoutCondition,
                                    "An expression was expected after the loop keyword '$conditionalKeyword' to act as the condition.") {
                                val fullText = parser.reader.readAllText()
                                addSourceCode(fullText, parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightCursorAt(parser.reader.currentPosition())
                                    message = "Try adding an expression here to act as the condition of the loop"
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    message = "Try removing the loop keyword '$conditionalKeyword'"
                                }
                            }

            WhitespaceNode.parse(parser)

            result.thenBlock = GlobalCommons.parseBlock(parser, result, ConditionalLoopStmtAnalyzer.signalEndThenBlock)
                    ?: throw AngmarParserException(AngmarParserExceptionType.ConditionalLoopStatementWithoutBlock,
                            "A block was expected after the condition expression to act as the code to be executed $conditionalKeyword the condition match.") {
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
                        LoopClausesStmtNode.parse(parser, result, ConditionalLoopStmtAnalyzer.signalEndLastClause)
                if (lastClauses == null) {
                    initLastClausesCursor.restore()
                }

                result.lastClauses = lastClauses
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
