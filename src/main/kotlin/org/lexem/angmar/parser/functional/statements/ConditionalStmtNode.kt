package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for conditional statements.
 */
class ConditionalStmtNode private constructor(parser: LexemParser, val condition: ParserNode,
        val thenBlock: ParserNode) : ParserNode(parser) {
    var isUnless = false
    var elseBlock: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(if (isUnless) {
            unlessKeyword
        } else {
            ifKeyword
        })

        append(' ')
        append(condition)
        append('\n')
        append(thenBlock)
        if (elseBlock != null) {
            append('\n')
            append(elseKeyword)
            append('\n')
            append(elseBlock)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("isUntil", isUnless)
        printer.addField("condition", condition)
        printer.addField("thenBlock", thenBlock)
        printer.addOptionalField("elseBlock", elseBlock)
    }

    companion object {
        const val ifKeyword = "if"
        const val unlessKeyword = "unless"
        const val elseKeyword = "else"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a conditional statement.
         */
        fun parse(parser: LexemParser): ConditionalStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ConditionalStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            var conditionalKeyword = ifKeyword
            val isUnless = when {
                Commons.parseKeyword(parser, unlessKeyword) -> {
                    conditionalKeyword = unlessKeyword
                    true
                }
                Commons.parseKeyword(parser, ifKeyword) -> false
                else -> return null
            }

            WhitespaceNode.parse(parser)

            val condition = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ConditionalStatementWithoutCondition,
                    "An expression was expected after the conditional keyword '$conditionalKeyword' to act as the condition.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an expression acting as the condition here")
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the conditional keyword '$conditionalKeyword'")
                }
            }

            WhitespaceNode.parse(parser)

            val thenBlock = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ConditionalStatementWithoutThenBlock,
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

            val result = ConditionalStmtNode(parser, condition, thenBlock)
            result.isUnless = isUnless

            // else block
            let {
                val initElseCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!Commons.parseKeyword(parser, elseKeyword)) {
                    initElseCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.elseBlock = parse(parser) ?: GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.ConditionalStatementWithoutElseBlock,
                        "A block was expected after the conditional keyword '$elseKeyword' to act as the code to be executed if the condition does not match.") {
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
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
