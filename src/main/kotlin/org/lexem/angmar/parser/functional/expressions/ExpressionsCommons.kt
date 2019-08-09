package org.lexem.angmar.parser.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*


/**
 * Commons for expressions.
 */
object ExpressionsCommons {
    const val operatorCharacters = "~!+-/<>*%&|^="

    /**
     * Parses an expression.
     */
    fun parseExpression(parser: LexemParser) = AssignExpressionNode.parse(parser) ?: parseRightExpression(parser)

    /**
     * Parses a macro.
     */
    fun parseMacro(parser: LexemParser) =
            MacroCheckProps.parse(parser) ?: MacroBacktrack.parse(parser) ?: MacroExpression.parse(parser)

    /**
     * Parses any literal.
     */
    fun parseLiteral(parser: LexemParser) =
            NilNode.parse(parser) ?: LogicNode.parse(parser) ?: LiteralCommons.parseAnyString(parser)
            ?: LiteralCommons.parseAnyInterval(parser) ?: BitlistNode.parse(parser)
            ?: NumberNode.parseAnyNumberDefaultDecimal(parser) ?: ListNode.parse(parser) ?: SetNode.parse(parser)
            ?: LiteralCommons.parseAnyObject(parser) ?: MapNode.parse(parser) ?: FunctionNode.parse(parser)

    /**
     * Parses a left expression.
     */
    fun parseLeftExpression(parser: LexemParser) = AccessExpressionNode.parse(parser)


    /**
     * Parses a right expression.
     */
    fun parseRightExpression(parser: LexemParser) = ConditionalExpressionNode.parse(parser)

    /**
     * Reads a valid operator.
     */
    fun readOperator(parser: LexemParser, operators: Sequence<String>,
            skipSuffixOperators: List<String>? = null): String? {
        val initCursor = parser.reader.saveCursor()

        val op = parser.readAnyText(operators) ?: return null

        // Final check.
        if (parser.checkAnyChar(operatorCharacters) != null) {
            val preWrongOperatorCursor = parser.reader.saveCursor()

            while (parser.readAnyChar(operatorCharacters) != null);

            val wrongOp = parser.substring(preWrongOperatorCursor, parser.reader.saveCursor())

            // If it is a prefix operator skip.
            if (wrongOp in PrefixOperatorNode.operators) {
                preWrongOperatorCursor.restore()
                return op
            }

            val fullOp = "$op$wrongOp"
            if (skipSuffixOperators != null && skipSuffixOperators.find { fullOp.startsWith(it) } != null) {
                initCursor.restore()
                return null
            }

            throw AngmarParserException(AngmarParserExceptionType.BinaryOperatorFollowedByAnotherOperator,
                    "The binary operator '$op' cannot be followed by another operator '$wrongOp'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(preWrongOperatorCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the wrong operator '$wrongOp' here")
                }
            }
        }

        return op
    }


    /**
     * Reads a valid prefix operator.
     */
    fun readPrefixOperator(parser: LexemParser, operators: Sequence<String>,
            skipSuffixOperators: List<String>? = null): String? {
        val initCursor = parser.reader.saveCursor()

        val op = parser.readAnyText(operators) ?: return null

        // Final check.
        if (parser.checkAnyChar(operatorCharacters) != null) {
            val preWrongOperatorCursor = parser.reader.saveCursor()

            while (parser.readAnyChar(operatorCharacters) != null);

            val wrongOp = parser.substring(preWrongOperatorCursor, parser.reader.saveCursor())

            val fullOp = "$op$wrongOp"
            if (skipSuffixOperators != null && skipSuffixOperators.find { fullOp.startsWith(it) } != null) {
                initCursor.restore()
                return null
            }

            throw AngmarParserException(AngmarParserExceptionType.PrefixOperatorFollowedByAnotherOperator,
                    "The prefix operator '$op' cannot be followed by another operator '$wrongOp'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(preWrongOperatorCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the wrong operator '$wrongOp' here")
                }
            }
        }

        return op
    }
}
