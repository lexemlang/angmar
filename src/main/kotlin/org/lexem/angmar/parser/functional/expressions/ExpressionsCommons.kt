package org.lexem.angmar.parser.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.literals.*


/**
 * Commons for expressions.
 */
internal object ExpressionsCommons {
    const val operatorCharacters = "~!+-/<>*%&|^="

    /**
     * Parses an expression.
     */
    fun parseExpression(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            AssignExpressionNode.parse(parser, parent, parentSignal) ?: RightExpressionNode.parse(parser, parent,
                    parentSignal)

    /**
     * Parses a macro.
     */
    fun parseMacro(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            MacroCheckProps.parse(parser, parent, parentSignal) ?: MacroBacktrack.parse(parser, parent, parentSignal)
            ?: MacroExpressionNode.parse(parser, parent, parentSignal)

    /**
     * Parses any literal.
     */
    fun parseLiteral(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            NilNode.parse(parser, parent, parentSignal) ?: LogicNode.parse(parser, parent, parentSignal)
            ?: LiteralCommons.parseAnyString(parser, parent, parentSignal) ?: LiteralCommons.parseAnyInterval(parser,
                    parent, parentSignal) ?: BitlistNode.parse(parser, parent, parentSignal)
            ?: NumberNode.parseAnyNumberDefaultDecimal(parser, parent, parentSignal) ?: ListNode.parse(parser, parent,
                    parentSignal) ?: SetNode.parse(parser, parent, parentSignal) ?: LiteralCommons.parseAnyObject(
                    parser, parent, parentSignal) ?: MapNode.parse(parser, parent, parentSignal) ?: FunctionNode.parse(
                    parser, parent, parentSignal)

    /**
     * Parses a left expression.
     */
    fun parseLeftExpression(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            AccessExpressionNode.parse(parser, parent, parentSignal)


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

            while (parser.readAnyChar(operatorCharacters) != null) {
            }

            val wrongOp = parser.reader.substring(preWrongOperatorCursor, parser.reader.saveCursor())

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
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(preWrongOperatorCursor.position(), parser.reader.currentPosition() - 1)
                    message = "Try removing the wrong operator '$wrongOp' here"
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

            while (parser.readAnyChar(operatorCharacters) != null) {
            }

            val wrongOp = parser.reader.substring(preWrongOperatorCursor, parser.reader.saveCursor())

            val fullOp = "$op$wrongOp"
            if (skipSuffixOperators != null && skipSuffixOperators.find { fullOp.startsWith(it) } != null) {
                initCursor.restore()
                return null
            }

            throw AngmarParserException(AngmarParserExceptionType.PrefixOperatorFollowedByAnotherOperator,
                    "The prefix operator '$op' cannot be followed by another operator '$wrongOp'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(preWrongOperatorCursor.position(), parser.reader.currentPosition() - 1)
                    message = "Try removing the wrong operator '$wrongOp' here"
                }
            }
        }

        return op
    }
}
