package org.lexem.angmar.parser.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for last clauses of loop statements.
 */
class LoopClausesStmtNode private constructor(parser: LexemParser) : ParserNode(parser) {
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

    override fun toTree(printer: TreeLikePrinter) {
        printer.addOptionalField("lastBlock", lastBlock)
        printer.addOptionalField("elseBlock", elseBlock)
    }

    companion object {
        const val lastKeyword = "last"
        const val elseKeyword = "else"


        // METHODS ------------------------------------------------------------

        /**
         * Parses last clauses of loop statements.
         */
        fun parse(parser: LexemParser): LoopClausesStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), LoopClausesStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = LoopClausesStmtNode(parser)

            var anyValue = false

            // last clause
            let {
                if (!Commons.parseKeyword(parser, lastKeyword)) {
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.lastBlock = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.LastLoopClauseWithoutBlock,
                        "A block was expected after the loop clause keyword '$lastKeyword'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here")
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        message("Try removing the '$lastKeyword' keyword")
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

                result.elseBlock = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.ElseLoopClauseWithoutBlock,
                        "A block was expected after the loop clause keyword '$elseKeyword'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here")
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        message("Try removing the '$elseKeyword' keyword")
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
