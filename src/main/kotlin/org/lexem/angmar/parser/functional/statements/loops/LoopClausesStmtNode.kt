package org.lexem.angmar.parser.functional.statements.loops

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.loops.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for last clauses of loop statements.
 */
internal class LoopClausesStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var lastBlock: ParserNode? = null
    var elseBlock: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        if (lastBlock != null) {
            append(lastKeyword)
            append(' ')
            append(lastBlock)
        }

        if (elseBlock != null) {
            if (lastBlock != null) {
                append('\n')
            }
            append(elseKeyword)
            append(' ')
            append(elseBlock)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("lastBlock", lastBlock?.toTree())
        result.add("elseBlock", elseBlock?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            LoopClausesStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        const val lastKeyword = "last"
        const val elseKeyword = "else"


        // METHODS ------------------------------------------------------------

        /**
         * Parses last clauses of loop statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode): LoopClausesStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = LoopClausesStmtNode(parser, parent)

            var anyValue = false

            // last clause
            let {
                if (!Commons.parseKeyword(parser, lastKeyword)) {
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.lastBlock = BlockStmtNode.parse(parser, result) ?: throw AngmarParserException(
                        AngmarParserExceptionType.LastLoopClauseWithoutBlock,
                        "A block was expected after the loop clause keyword '$lastKeyword'.") {
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
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        message = "Try removing the '$lastKeyword' keyword"
                    }
                }

                anyValue = true
            }

            // else clause
            let {
                val initElseClauseCursor = parser.reader.saveCursor()

                if (anyValue) {
                    WhitespaceNode.parse(parser)
                }

                if (!Commons.parseKeyword(parser, elseKeyword)) {
                    initElseClauseCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.elseBlock = BlockStmtNode.parse(parser, result) ?: throw AngmarParserException(
                        AngmarParserExceptionType.ElseLoopClauseWithoutBlock,
                        "A block was expected after the loop clause keyword '$elseKeyword'.") {
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
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        message = "Try removing the '$elseKeyword' keyword"
                    }
                }

                anyValue = true
            }

            if (!anyValue) {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
