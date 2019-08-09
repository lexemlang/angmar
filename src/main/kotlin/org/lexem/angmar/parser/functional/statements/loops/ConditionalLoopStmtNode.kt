package org.lexem.angmar.parser.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for conditional loop statements.
 */
class ConditionalLoopStmtNode private constructor(parser: LexemParser, val condition: ParserNode,
        val thenBlock: ParserNode) : ParserNode(parser) {
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

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("isUntil", isUntil)
        printer.addOptionalField("index", index)
        printer.addField("condition", condition)
        printer.addField("thenBlock", thenBlock)
        printer.addOptionalField("lastClauses", lastClauses)
    }

    companion object {
        const val whileKeyword = "while"
        const val untilKeyword = "until"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a conditional loop statement.
         */
        fun parse(parser: LexemParser): ConditionalLoopStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ConditionalLoopStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            // index
            var index: IdentifierNode? = null
            let {
                if (!Commons.parseKeyword(parser, InfiniteLoopStmtNode.keyword)) {
                    return@let
                }

                WhitespaceNode.parse(parser)

                index = IdentifierNode.parse(parser)
                if (index == null) {
                    initCursor.restore()
                    return null
                }

                WhitespaceNode.parse(parser)
            }

            var conditionalKeyword = whileKeyword
            val isUntil = when {
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

            val condition = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ConditionalLoopStatementWithoutCondition,
                    "An expression was expected after the loop keyword '$conditionalKeyword' to act as the condition.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an expression here to act as the condition of the loop")
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the loop keyword '$conditionalKeyword'")
                }
            }

            WhitespaceNode.parse(parser)

            val thenBlock = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ConditionalLoopStatementWithoutBlock,
                    "A block was expected after the condition expression to act as the code to be executed $conditionalKeyword the condition match.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here")
                }
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

            val result = ConditionalLoopStmtNode(parser, condition, thenBlock)
            result.isUntil = isUntil
            result.index = index
            result.lastClauses = lastClauses

            return parser.finalizeNode(result, initCursor)
        }
    }
}
