package org.lexem.angmar.nodes.expressions

import es.jtp.kterm.Logger
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.Consts
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.WhitespaceNoEOLNode
import org.lexem.angmar.nodes.commons.WhitespaceNode
import org.lexem.angmar.nodes.expressions.operators.ExplicitPrefixOperatorsNode
import org.lexem.angmar.nodes.expressions.operators.InfixOperatorNode


/**
 * Parser for expressions.
 */
class ExpressionNode private constructor(parser: LexemParser, val element: ExpressionElementWithPostfixNode) :
        ParserNode(nodeType, parser) {
    var prefixOperators: ExplicitPrefixOperatorsNode? = null
    var prefixWhitespace: WhitespaceNoEOLNode? = null
    var preInfixWhitespace: WhitespaceNoEOLNode? = null
    var infixOperator: InfixOperatorNode? = null
    var preExpressionWhitespace: WhitespaceNode? = null
    var nextExpression: ExpressionNode? = null

    override fun toString() = StringBuilder().apply {
        if (prefixOperators != null) {
            append(prefixOperators)
            append(prefixWhitespace)
        }

        append(element)

        if (infixOperator != null) {
            append(preInfixWhitespace)
            append(infixOperator)
            append(preExpressionWhitespace)
            append(nextExpression)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        if (prefixOperators != null) {
            printer.addOptionalField("prefixOperators", prefixOperators)
            printer.addOptionalField("prefixWhitespace", prefixWhitespace)
        }

        printer.addOptionalField("element", element)

        if (infixOperator != null) {
            printer.addOptionalField("preInfixWhitespace", preInfixWhitespace)
            printer.addOptionalField("infixOperator", infixOperator)
            printer.addOptionalField("preExpressionWhitespace", preExpressionWhitespace)
            printer.addOptionalField("nextExpression", preExpressionWhitespace)
        }
    }

    companion object {
        private val nodeType = NodeType.Expression

        // METHODS ------------------------------------------------------------

        /**
         * Parses an expression.
         */
        fun parse(parser: LexemParser): ExpressionNode? {
            parser.fromBuffer<ExpressionNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val prefixOperators = ExplicitPrefixOperatorsNode.parse(parser)
            val result = if (prefixOperators != null) {
                val prefixWhitespace = WhitespaceNoEOLNode.parse(parser)
                val element = ExpressionElementWithPostfixNode.parse(parser) ?: let {
                    throw AngmarParserException(
                        Logger.build("It was expected an expression after the prefix '$prefixOperators'.") {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightAt(prefixOperators.from.position(), prefixOperators.to.position())
                                message("Try removing the prefix operator")
                            }
                        })
                }

                val result = ExpressionNode(parser, element)
                result.prefixOperators = prefixOperators
                result.prefixWhitespace = prefixWhitespace
                result
            } else {
                val element = ExpressionElementWithPostfixNode.parse(parser) ?: return null
                ExpressionNode(parser, element)
            }

            val preInfixCursor = parser.reader.saveCursor()

            val preInfixWhitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)
            val infixOperator = InfixOperatorNode.parse(parser)

            if (infixOperator != null) {
                val preExpressionWhitespace = WhitespaceNode.parseOrEmpty(parser)
                val nextExpression = parse(parser) ?: let {
                    throw AngmarParserException(
                        Logger.build("It was expected an expression after the infix operator '$infixOperator'.") {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightAt(infixOperator.from.position(), infixOperator.to.position())
                                message("Try removing the infix operator")
                            }
                        })
                }

                result.preInfixWhitespace = preInfixWhitespace
                result.infixOperator = infixOperator
                result.preExpressionWhitespace = preExpressionWhitespace
                result.nextExpression = nextExpression
            } else {
                preInfixCursor.restore()
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}