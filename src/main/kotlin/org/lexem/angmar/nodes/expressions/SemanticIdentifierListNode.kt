package org.lexem.angmar.nodes.expressions

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.commons.IdentifierNode
import org.lexem.angmar.nodes.commons.WhitespaceNode
import org.lexem.angmar.nodes.expressions.operators.GraphicOperatorNode

/**
 * Parser for semantic-resolved identifier list.
 */
class SemanticIdentifierListNode private constructor(parser: LexemParser) : ParserNode(nodeType, parser) {
    val identifiers = mutableListOf<IdentifierNode>()
    val whitespaces = mutableListOf<WhitespaceNode>()

    override fun toString() = StringBuilder().apply {
        append(identifiers.first())

        for (i in 1 until identifiers.size) {
            append(whitespaces[i - 1])
            append(identifiers[i])
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("identifiers", identifiers)
        printer.addField("whitespaces", whitespaces)
    }

    companion object {
        private val nodeType = NodeType.SemanticIdentifierList

        // METHODS ------------------------------------------------------------

        /**
         * Parser a semantic-resolved identifier list.
         */
        fun parse(parser: LexemParser): SemanticIdentifierListNode? {
            parser.fromBuffer<SemanticIdentifierListNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (IdentifierNode.parseAnyKeyword(parser) != null) {
                initCursor.restore()
                return null
            }

            val result = SemanticIdentifierListNode(parser)

            val id = IdentifierNode.parse(parser) ?: return null
            if (parser.readText(GraphicOperatorNode.operatorSeparator) || ExpressionsCommons.parseElementModifier(
                            parser) != null) {
                initCursor.restore()
                return null
            }
            result.identifiers.add(id)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()
                val whitespace = WhitespaceNode.parseOrEmpty(parser)

                if (IdentifierNode.parseAnyKeyword(parser) != null) {
                    initLoopCursor.restore()
                    break
                }


                val id2 = IdentifierNode.parse(parser)
                if (id2 == null || parser.readText(
                                GraphicOperatorNode.operatorSeparator) || ExpressionsCommons.parseElementModifier(
                                parser) != null) {
                    initLoopCursor.restore()
                    break
                }

                result.identifiers.add(id2)
                result.whitespaces.add(whitespace)
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}