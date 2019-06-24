package org.lexem.angmar.nodes.expressions.operators

import es.jtp.kterm.Logger
import org.lexem.angmar.LexemParser
import org.lexem.angmar.config.Consts
import org.lexem.angmar.errors.AngmarParserException
import org.lexem.angmar.io.ITextReaderCursor
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.CommentMultilineNode
import org.lexem.angmar.nodes.commons.IdentifierNode
import org.lexem.angmar.nodes.commons.WhitespaceNoEOLNode
import org.lexem.angmar.nodes.commons.WhitespaceNode


/**
 * Parser for postfix operators.
 */
class ExplicitPostfixOperatorsNode private constructor(parser: LexemParser) :
    ParserNode(nodeType, parser) {
    var operators = mutableListOf<ParserNode>()
    var whitespaces = mutableListOf<WhitespaceNoEOLNode>()

    override fun toString() = StringBuilder().apply {
        if (operators.first() is IdentifierNode) {
            append(GraphicOperatorNode.operatorSeparator)
        }
        append(operators.first())

        for (i in 1 until operators.size) {
            append(whitespaces[i - 1])
            append(GraphicOperatorNode.operatorSeparator)
            append(operators[i])
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("operators", operators)
        printer.addField("whitespaces", whitespaces)
    }

    companion object {
        private val nodeType = NodeType.PostfixOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a postfix operator.
         */
        fun parse(parser: LexemParser): ExplicitPostfixOperatorsNode? {
            parser.fromBuffer<ExplicitPostfixOperatorsNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ExplicitPostfixOperatorsNode(parser)

            var operator = GraphicOperatorNode.parse(parser)
            when {
                operator != null -> {
                    result.operators.add(operator)
                }
                parser.readText(GraphicOperatorNode.operatorSeparator) -> {
                    val id = IdentifierNode.parse(parser) ?: let {
                        // Check if there is a graphic operator at first position.
                        operator = GraphicOperatorNode.parse(parser)
                        if (operator != null) {
                            throw AngmarParserException(
                                Logger.build(
                                    "The graphic postfix operator at first position can't have the Operator Separator character '\\${GraphicOperatorNode.operatorSeparator}' before it."
                                ) {
                                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                        title(Consts.Logger.codeTitle)
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(parser.reader.readAllText(), null) {
                                        title(Consts.Logger.hintTitle)
                                        highlightAt(initCursor.position())
                                        message("Try removing the Operator Separator character '\\${GraphicOperatorNode.operatorSeparator}' here")
                                    }
                                })
                        } else {
                            throw AngmarParserException(Logger.build(
                                "Postfix operators at first position require an identifier after the Operator Separator character '\\${GraphicOperatorNode.operatorSeparator}'."
                            ) {
                                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                    title(Consts.Logger.codeTitle)
                                    highlightSection(initCursor.position(), parser.reader.currentPosition())
                                }
                                addSourceCode(parser.reader.readAllText(), null) {
                                    title(Consts.Logger.hintTitle)
                                    highlightAt(initCursor.position() + 1)
                                    message("Try removing the Operator Separator character '\\${GraphicOperatorNode.operatorSeparator}' here")
                                }
                            })
                        }
                    }
                    result.operators.add(id)
                }
                else -> return null
            }

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()
                val whitespace = WhitespaceNoEOLNode.parseOrEmpty(parser)

                if (!parser.readText(GraphicOperatorNode.operatorSeparator)) {
                    initLoopCursor.restore()
                    break
                }

                val operator2 = GraphicOperatorNode.parse(parser) ?: IdentifierNode.parse(parser)
                ?: throw AngmarParserException(Logger.build(
                    "Explicit postfix operators require a graphic operator or an identifier after the Operator Separator character '\\${GraphicOperatorNode.operatorSeparator}'."
                ) {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightAt(parser.reader.currentPosition() - 1)
                        message("Try removing the Operator Separator character '\\${GraphicOperatorNode.operatorSeparator}' here")
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightAt(parser.reader.currentPosition())
                        message("Try adding a graphic operator (++, --) or an identifier (px, inch) here")
                    }
                })

                result.operators.add(operator2)
                result.whitespaces.add(whitespace)
            }

            // If there is only one operator and it is graphic, it is necessary
            // to check whether is immediately after the element.
            if (result.operators.size == 1 && result.operators.first() is GraphicOperatorNode && !checkWhitespaceBeforeTheFirstOperator(
                    parser, result.operators.first().from
                )
            ) {
                initCursor.restore()
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Checks whether the current position is preceded by a whitespace or comment.
         */
        private fun checkWhitespaceBeforeTheFirstOperator(
            parser: LexemParser,
            initPosition: ITextReaderCursor
        ): Boolean {
            val finalPosition = parser.reader.saveCursor()

            initPosition.restore()
            if (!parser.reader.back()) {
                finalPosition.restore()
                return false
            }

            // Check whitespace.
            if (parser.readAnyChar(WhitespaceNode.whitespaceChars) ?: parser.readAnyChar(
                    WhitespaceNode.endOfLineChars
                ) != null
            ) {
                finalPosition.restore()
                return false
            }

            // Check multiline comment.
            parser.reader.back(CommentMultilineNode.MultilineEndToken.length - 1)
            if (parser.readText(CommentMultilineNode.MultilineEndToken)) {
                finalPosition.restore()
                return false
            }

            finalPosition.restore()
            return true
        }
    }
}