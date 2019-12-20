package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for conditional statements.
 */
internal class ConditionalStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var condition: ParserNode
    lateinit var thenBlock: ParserNode
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

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isUntil", isUnless)
        result.add("condition", condition.toTree())
        result.add("thenBlock", thenBlock.toTree())
        result.add("elseBlock", elseBlock?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ConditionalStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val ifKeyword = "if"
        const val unlessKeyword = "unless"
        const val elseKeyword = "else"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a conditional statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ConditionalStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ConditionalStmtNode(parser, parent, parentSignal)

            var conditionalKeyword = ifKeyword
            result.isUnless = when {
                Commons.parseKeyword(parser, unlessKeyword) -> {
                    conditionalKeyword = unlessKeyword
                    true
                }
                Commons.parseKeyword(parser, ifKeyword) -> false
                else -> return null
            }

            WhitespaceNode.parse(parser)

            result.condition =
                    ExpressionsCommons.parseExpression(parser, result, ConditionalStmtAnalyzer.signalEndCondition)
                            ?: throw AngmarParserException(
                                    AngmarParserExceptionType.ConditionalStatementWithoutCondition,
                                    "An expression was expected after the conditional keyword '$conditionalKeyword' to act as the condition.") {
                                val fullText = parser.reader.readAllText()
                                addSourceCode(fullText, parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightCursorAt(parser.reader.currentPosition())
                                    message = "Try adding an expression acting as the condition here"
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    message = "Try removing the conditional keyword '$conditionalKeyword'"
                                }
                            }

            WhitespaceNode.parse(parser)

            result.thenBlock = BlockStmtNode.parse(parser, result, ConditionalStmtAnalyzer.signalEndThenBlock)
                    ?: throw AngmarParserException(AngmarParserExceptionType.ConditionalStatementWithoutThenBlock,
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

            // else block
            let {
                val initElseCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!Commons.parseKeyword(parser, elseKeyword)) {
                    initElseCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.elseBlock =
                        parse(parser, result, ConditionalStmtAnalyzer.signalEndElseBlock) ?: BlockStmtNode.parse(parser,
                                result, ConditionalStmtAnalyzer.signalEndElseBlock) ?: throw AngmarParserException(
                                AngmarParserExceptionType.ConditionalStatementWithoutElseBlock,
                                "A block was expected after the conditional keyword '$elseKeyword' to act as the code to be executed if the condition does not match.") {
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
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
