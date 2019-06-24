package org.lexem.angmar.nodes.expressions.operators

import org.lexem.angmar.LexemParser
import org.lexem.angmar.io.printer.TreeLikePrinter
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode


/**
 * Parser for a graphic operator.
 */
class GraphicOperatorNode private constructor(parser: LexemParser, var operator: String) :
        ParserNode(nodeType, parser) {

    override fun toString() = operator

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("operator", operator)
    }

    companion object {
        private val nodeType = NodeType.GraphicOperator
        val operatorHeadChars = listOf('\u0021'..'\u0021', '\u0025'..'\u0026', '\u002A'..'\u002B', '\u002D'..'\u002D',
                '\u002F'..'\u002F', '\u003C'..'\u003F', '\u005E'..'\u005E', '\u007C'..'\u007C', '\u007E'..'\u007E',
                '\u00A1'..'\u00A7', '\u00A9'..'\u00A9', '\u00AB'..'\u00AC', '\u00AE'..'\u00AE', '\u00B0'..'\u00B1',
                '\u00B6'..'\u00B6', '\u00BB'..'\u00BB', '\u00BF'..'\u00BF', '\u00D7'..'\u00D7', '\u00F7'..'\u00F7',
                '\u2016'..'\u2017', '\u2020'..'\u2027', '\u2030'..'\u203E', '\u2041'..'\u2053', '\u2055'..'\u205E',
                '\u2190'..'\u23FF', '\u2500'..'\u2775', '\u2794'..'\u2BFF', '\u2E00'..'\u2E7F', '\u3001'..'\u3003',
                '\u3008'..'\u3020', '\u3030'..'\u3030')
        val operatorEndChars = listOf('\u0300'..'\u036F', '\u1DC0'..'\u1DFF', '\u20D0'..'\u20FF', '\uFE00'..'\uFE0F',
                '\uFE20'..'\uFE2F')
        const val dotOperatorChar = "."
        const val operatorSeparator = "'"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a graphic operator.
         */
        fun parse(parser: LexemParser): GraphicOperatorNode? {
            parser.fromBuffer<GraphicOperatorNode>(parser.reader.currentPosition(), nodeType)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val operator = StringBuilder()

            // Dot operator must be followed by another operator: .., ..>
            if (parser.readText(dotOperatorChar)) {
                operator.append(dotOperatorChar)

                operator.append(parser.readAnyChar(dotOperatorChar) ?: parser.readAnyChar(operatorHeadChars)
                ?: parser.readAnyChar(operatorEndChars) ?: let {
                    initCursor.restore()
                    return@parse null
                })
            } else {
                operator.append(parser.readAnyChar(operatorHeadChars) ?: return null)
            }

            while (true) {
                operator.append(parser.readAnyChar(dotOperatorChar) ?: parser.readAnyChar(operatorHeadChars)
                ?: parser.readAnyChar(operatorEndChars) ?: break)
            }

            val result = GraphicOperatorNode(parser, operator.toString())
            return parser.finalizeNode(result, initCursor)
        }
    }
}