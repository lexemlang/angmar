package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for interval literals.
 */
class IntervalNode private constructor(parser: LexemParser) : ParserNode(parser) {
    val elements = mutableListOf<ParserNode>()
    var reversed = false

    override fun toString() = StringBuilder().apply {
        append(macroName)
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
        const val macroName = "itv${MacroExpression.macroSuffix}"
        const val startToken = "["
        const val reversedToken = GlobalCommons.notToken
        const val endToken = "]"


        // METHODS ------------------------------------------------------------

        /**
         * Parses an interval literal.
         */
        fun parse(parser: LexemParser): IntervalNode? {
            parser.fromBuffer(parser.reader.currentPosition(), IntervalNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = IntervalNode(parser)

            if (!parser.readText(macroName)) {
                return null
            }

            if (!parser.readText(startToken)) {
                throw AngmarParserException(AngmarParserExceptionType.IntervalWithoutStartToken,
                        "The start square bracket was expected '$startToken'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the start and end square brackets '$startToken$endToken' here")
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        message("Try removing the '$macroName' macro")
                    }
                }
            }

            result.reversed = parser.readText(reversedToken)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parseSimpleWhitespaces(parser)

                val node = IntervalSubIntervalNode.parse(parser) ?: IntervalElementNode.parse(parser)
                if (node == null) {
                    initLoopCursor.restore()
                    break
                }

                result.elements.add(node)
            }

            WhitespaceNode.parseSimpleWhitespaces(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.IntervalWithoutEndToken,
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
