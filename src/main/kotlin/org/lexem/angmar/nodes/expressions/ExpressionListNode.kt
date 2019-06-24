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


/**
 * Parser for expression list.
 */
class ExpressionListNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var expressions = mutableListOf<ExpressionNode>()
    var whitespaces = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(expressions.first())
        for (i in 1 until expressions.size) {
            val whitespaceIndex = (i - 1) * 2
            append(whitespaces[whitespaceIndex])
            append(expressionSeparator)
            append(whitespaces[whitespaceIndex + 1])
            append(expressions[i])
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("expressions", expressions)
        printer.addField("whitespaces", whitespaces)
    }

    companion object {
        private val nodeType = NodeType.ExpressionList

        private const val expressionSeparator = ","

        // METHODS ------------------------------------------------------------

        /**
         * Parses an expression list.
         */
        fun parse(parser: LexemParser): ExpressionListNode? {
            parser.fromBuffer<ExpressionListNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ExpressionListNode(parser)

            result.expressions.add(ExpressionNode.parse(parser) ?: return null)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                val preSeparatorWhitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)

                if (!parser.readText(expressionSeparator)) {
                    initLoopCursor.restore()
                    break
                }

                val preExpressionWhitespace = WhitespaceNode.parseOrEmpty(parser)

                val expression = ExpressionNode.parse(parser) ?: let {
                    throw AngmarParserException(
                        Logger.build(
                        "It was expected an expression, after the expression separator token '$expressionSeparator'."
                    ) {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightAt(preSeparatorWhitespace.to.position())
                            message("Try removing the expression separator token '$expressionSeparator'.")
                        }
                    })
                }

                result.whitespaces.add(preSeparatorWhitespace)
                result.whitespaces.add(preExpressionWhitespace)
                result.expressions.add(expression)
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}