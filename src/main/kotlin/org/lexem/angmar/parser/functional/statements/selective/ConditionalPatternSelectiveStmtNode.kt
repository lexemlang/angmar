package org.lexem.angmar.parser.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for conditional patterns of the selective statements.
 */
class ConditionalPatternSelectiveStmtNode private constructor(parser: LexemParser, val condition: ParserNode) :
        ParserNode(parser) {
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

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("isUntil", isUnless)
        printer.addField("condition", condition)
    }

    companion object {
        const val ifKeyword = ConditionalStmtNode.ifKeyword
        const val unlessKeyword = ConditionalStmtNode.unlessKeyword


        // METHODS ------------------------------------------------------------

        /**
         * Parses a conditional pattern of the selective statements.
         */
        fun parse(parser: LexemParser): ConditionalPatternSelectiveStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ConditionalPatternSelectiveStmtNode::class.java)?.let {
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
                    AngmarParserExceptionType.ConditionalPatternSelectiveStatementWithoutCondition,
                    "An expression was expected after the conditional keyword '$conditionalKeyword' to act as the condition.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the conditional keyword '$conditionalKeyword'")
                }
            }

            val result = ConditionalPatternSelectiveStmtNode(parser, condition)
            result.isUnless = isUnless

            return parser.finalizeNode(result, initCursor)
        }
    }
}
