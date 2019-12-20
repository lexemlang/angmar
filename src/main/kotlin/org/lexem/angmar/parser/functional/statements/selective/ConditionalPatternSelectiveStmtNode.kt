package org.lexem.angmar.parser.functional.statements.selective

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.selective.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for conditional patterns of the selective statements.
 */
internal class ConditionalPatternSelectiveStmtNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var condition: ParserNode
    var isUnless = false

    override fun toString() = StringBuilder().apply {
        append(if (isUnless) {
            unlessKeyword
        } else {
            ifKeyword
        })
        append(' ')
        append(condition)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isUntil", isUnless)
        result.add("condition", condition.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ConditionalPatternSelectiveStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val ifKeyword = ConditionalStmtNode.ifKeyword
        const val unlessKeyword = ConditionalStmtNode.unlessKeyword


        // METHODS ------------------------------------------------------------

        /**
         * Parses a conditional pattern of the selective statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ConditionalPatternSelectiveStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ConditionalPatternSelectiveStmtNode(parser, parent, parentSignal)

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

            result.condition = ExpressionsCommons.parseExpression(parser, result,
                    ConditionalPatternSelectiveStmtAnalyzer.signalEndCondition) ?: throw AngmarParserException(
                    AngmarParserExceptionType.ConditionalPatternSelectiveStatementWithoutCondition,
                    "An expression was expected after the conditional keyword '$conditionalKeyword' to act as the condition.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message = "Try removing the conditional keyword '$conditionalKeyword'"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
