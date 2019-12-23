package org.lexem.angmar.parser.functional.statements.loops

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.loops.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for iterator loop statements.
 */
internal class IteratorLoopStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var variable: ParserNode
    lateinit var condition: ParserNode
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

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("index", index?.toTree())
        result.add("variable", variable.toTree())
        result.add("condition", condition.toTree())
        result.add("thenBlock", thenBlock.toTree())
        result.add("lastClauses", lastClauses?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            IteratorLoopStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        const val keyword = "for"
        const val relationKeyword = "in"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a iterator loop statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode): IteratorLoopStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = IteratorLoopStmtNode(parser, parent)

            // index
            let {
                if (!Commons.parseKeyword(parser, InfiniteLoopStmtNode.keyword)) {
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.index = IdentifierNode.parse(parser, result)
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

            result.variable =
                    DestructuringStmtNode.parse(parser, result) ?: Commons.parseDynamicIdentifier(parser, result)
                            ?: throw AngmarParserException(
                            AngmarParserExceptionType.IteratorLoopStatementWithoutIdentifier,
                            "An identifier or a destructuring was expected after the loop keyword '$keyword' to act as the iterator receiver.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding the wildcard variable '${GlobalCommons.wildcardVariable}' here"
                        }
                    }

            WhitespaceNode.parse(parser)

            if (!Commons.parseKeyword(parser, relationKeyword)) {
                throw AngmarParserException(AngmarParserExceptionType.IteratorLoopStatementWithoutRelationalKeyword,
                        "The '$relationKeyword' keyword was expected after the variable declaration of the iterator loop.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the '$relationKeyword' keyword here"
                    }
                }
            }

            WhitespaceNode.parse(parser)

            result.condition = ExpressionsCommons.parseExpression(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.IteratorLoopStatementWithoutExpressionAfterRelationalKeyword,
                    "An expression was expected after the loop keyword '$keyword' to act as the condition.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message = "Try adding an expression here"
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message = "Try removing the loop"
                }
            }

            WhitespaceNode.parse(parser)

            result.thenBlock = BlockStmtNode.parse(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.IteratorLoopStatementWithoutBlock,
                    "A block was expected after the condition expression to act as the code to be executed if the condition match.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message = "Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here"
                }
            }

            // last clauses
            let {
                val initLastClausesCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                result.lastClauses = LoopClausesStmtNode.parse(parser, result)
                if (result.lastClauses == null) {
                    initLastClausesCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
