package org.lexem.angmar.nodes.expressions.operators

import es.jtp.kterm.Logger
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.Consts
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.IdentifierNode
import org.lexem.angmar.nodes.commons.WhitespaceNoEOLNode


/**
 * Parser for prefix operators.
 */
class ExplicitPrefixOperatorsNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    var operators = mutableListOf<ParserNode>()
    var whitespaces = mutableListOf<WhitespaceNoEOLNode>()

    override fun toString() = StringBuilder().apply {
        for (i in 0 until operators.size - 1) {
            append(operators[i])
            append(GraphicOperatorNode.operatorSeparator)
            append(whitespaces[i])
        }

        append(operators.last())
        if (operators.last() is IdentifierNode) {
            append(GraphicOperatorNode.operatorSeparator)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("operators", operators)
        printer.addField("whitespaces", whitespaces)
    }

    companion object {
        private val nodeType = NodeType.PrefixOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a prefix operator.
         */
        fun parse(parser: LexemParser): ExplicitPrefixOperatorsNode? {
            parser.fromBuffer<ExplicitPrefixOperatorsNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ExplicitPrefixOperatorsNode(parser)

            var requiredByGraphicOperator = false
            var whitespace: WhitespaceNoEOLNode
            var operator: ParserNode?

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()
                whitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)

                operator = GraphicOperatorNode.parse(parser)
                if (operator != null) {
                    if (!parser.readText(GraphicOperatorNode.operatorSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    requiredByGraphicOperator = true
                } else {
                    operator = IdentifierNode.parse(parser)

                    if (operator == null || !parser.readText(GraphicOperatorNode.operatorSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    requiredByGraphicOperator = false
                }

                if (result.operators.isNotEmpty()) {
                    result.whitespaces.add(whitespace)
                }
                result.operators.add(operator)
            }

            val lastPostfixCursor = parser.reader.saveCursor()
            whitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)

            operator = GraphicOperatorNode.parse(parser)
            if (operator != null) {
                if (result.operators.isNotEmpty()) {
                    result.whitespaces.add(whitespace)
                }
                result.operators.add(operator)
                requiredByGraphicOperator = false
            } else {
                lastPostfixCursor.restore()
            }

            if (requiredByGraphicOperator) {
                throw AngmarParserException(
                    Logger.build(
                    "The prefix graphic operator (${result.operators.last()}) requires another operator after it to finish the sequence."
                ) {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightAt(result.operators.last().to.position())
                        message("Try removing the Operator Separator character (${GraphicOperatorNode.operatorSeparator}) here")
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightAt(parser.reader.currentPosition())
                        message("Try adding another graphic operator here without the Operator Separator character (${GraphicOperatorNode.operatorSeparator}) or a textual operator with it")
                    }
                })
            }

            if (result.operators.isEmpty()) {
                initCursor.restore()
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}