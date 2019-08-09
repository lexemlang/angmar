package org.lexem.angmar.parser.functional.expressions

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*


/**
 * Parser for prefix expression.
 */
class PrefixExpressionNode private constructor(parser: LexemParser, var prefix: ParserNode, var element: ParserNode) :
        ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(prefix)
        append(element)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("macroName", prefix)
        printer.addField("element", element)
    }

    companion object {

        /**
         * Parses a prefix expression.
         */
        fun parse(parser: LexemParser): ParserNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PrefixExpressionNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val prefix = PrefixOperatorNode.parse(parser)
            val element = AccessExpressionNode.parse(parser) ?: let {
                if (prefix == null) {
                    return@parse null
                }

                throw AngmarParserException(
                        AngmarParserExceptionType.PrefixExpressionWithoutExpressionAfterThePrefixOperator,
                        "An infix expression was expected after the prefix operator '$prefix'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightSection(prefix.from.position(), prefix.to.position() - 1)
                        message("Try removing the prefix operator")
                    }
                }
            }

            if (prefix == null) {
                return element
            }

            val result = PrefixExpressionNode(parser, prefix, element)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
