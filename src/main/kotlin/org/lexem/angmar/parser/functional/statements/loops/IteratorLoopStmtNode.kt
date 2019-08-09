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
 * Parser for iterator loop statements.
 */
class IteratorLoopStmtNode private constructor(parser: LexemParser, val variable: ParserNode, val condition: ParserNode,
        val thenBlock: ParserNode) : ParserNode(parser) {
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
        append(variable)
        append(' ')
        append(relationKeyword)
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
        printer.addOptionalField("index", index)
        printer.addField("variable", variable)
        printer.addField("condition", condition)
        printer.addField("thenBlock", thenBlock)
        printer.addOptionalField("lastClauses", lastClauses)
    }

    companion object {
        const val keyword = "for"
        const val relationKeyword = "in"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a iterator loop statement.
         */
        fun parse(parser: LexemParser): IteratorLoopStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), IteratorLoopStmtNode::class.java)?.let {
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

            if (!Commons.parseKeyword(parser, keyword)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            val variable = DestructuringStmtNode.parse(parser) ?: Commons.parseDynamicIdentifier(parser)
            ?: throw AngmarParserException(AngmarParserExceptionType.IteratorLoopStatementWithoutIdentifier,
                    "An identifier or a destructuring was expected after the loop keyword '$keyword' to act as the iterator receiver.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding the wildcard variable '${GlobalCommons.wildcardVariable}' here")
                }
            }

            WhitespaceNode.parse(parser)

            if (!Commons.parseKeyword(parser, relationKeyword)) {
                throw AngmarParserException(AngmarParserExceptionType.IteratorLoopStatementWithoutRelationalKeyword,
                        "The '$relationKeyword' keyword was expected after the variable declaration of the iterator loop.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the '$relationKeyword' keyword here")
                    }
                }
            }

            WhitespaceNode.parse(parser)

            val condition = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.IteratorLoopStatementWithoutExpressionAfterRelationalKeyword,
                    "An expression was expected after the loop keyword '$keyword' to act as the condition.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an expression here")
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the loop")
                }
            }

            WhitespaceNode.parse(parser)

            val thenBlock = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.IteratorLoopStatementWithoutBlock,
                    "A block was expected after the condition expression to act as the code to be executed if the condition match.") {
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

            val result = IteratorLoopStmtNode(parser, variable, condition, thenBlock)
            result.index = index
            result.lastClauses = lastClauses

            return parser.finalizeNode(result, initCursor)
        }
    }
}
