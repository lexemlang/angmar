package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for sub-intervals of unicode interval literals.
 */
class UnicodeIntervalSubIntervalNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var operator = IntervalSubIntervalNode.Operator.Add
    var reversed = false
    val elements = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(startToken)
        append(operator.operator)
        if (reversed) {
            append(reversedToken)
        }
        append(elements.joinToString(" "))
        append(endToken)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("elements", elements)
        printer.addField("reversed", reversed)
    }

    companion object {
        const val startToken = "["
        const val reversedToken = GlobalCommons.notToken
        const val endToken = "]"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a sub-interval of unicode interval literals.
         */
        fun parse(parser: LexemParser): UnicodeIntervalSubIntervalNode? {
            parser.fromBuffer(parser.reader.currentPosition(), UnicodeIntervalSubIntervalNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = UnicodeIntervalSubIntervalNode(parser)

            if (!parser.readText(startToken)) {
                return null
            }

            val operator = when {
                parser.readText(IntervalSubIntervalNode.Operator.Add.operator) -> IntervalSubIntervalNode.Operator.Add
                parser.readText(IntervalSubIntervalNode.Operator.Sub.operator) -> IntervalSubIntervalNode.Operator.Sub
                parser.readText(
                        IntervalSubIntervalNode.Operator.Common.operator) -> IntervalSubIntervalNode.Operator.Common
                parser.readText(
                        IntervalSubIntervalNode.Operator.NotCommon.operator) -> IntervalSubIntervalNode.Operator.NotCommon
                else -> null
            }

            if (operator != null) {
                result.operator = operator
                result.reversed = parser.readText(reversedToken)
            }

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parseSimpleWhitespaces(parser)

                val node = parse(parser) ?: UnicodeIntervalElementNode.parse(parser)
                if (node == null) {
                    initLoopCursor.restore()
                    break
                }

                result.elements.add(node)
            }

            WhitespaceNode.parseSimpleWhitespaces(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.UnicodeIntervalSubIntervalWithoutEndToken,
                        "The close square bracket was expected '$endToken'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the close square bracket '$endToken' here")
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
