package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for abbreviated unicode interval literals.
 */
class UnicodeIntervalAbbrNode private constructor(parser: LexemParser) : ParserNode(parser) {
    val elements = mutableListOf<ParserNode>()
    var reversed = false

    override fun toString() = StringBuilder().apply {
        append(startToken)
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
         * Parses an abbreviated unicode interval literal.
         */
        fun parse(parser: LexemParser): UnicodeIntervalAbbrNode? {
            parser.fromBuffer(parser.reader.currentPosition(), UnicodeIntervalAbbrNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = UnicodeIntervalAbbrNode(parser)

            if (!parser.readText(startToken)) {
                return null
            }

            result.reversed = parser.readText(reversedToken)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parseSimpleWhitespaces(parser)

                val node = UnicodeIntervalSubIntervalNode.parse(parser) ?: UnicodeIntervalElementNode.parse(parser)
                if (node == null) {
                    initLoopCursor.restore()
                    break
                }

                result.elements.add(node)
            }

            WhitespaceNode.parseSimpleWhitespaces(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.UnicodeIntervalAbbreviationWithoutEndToken,
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
