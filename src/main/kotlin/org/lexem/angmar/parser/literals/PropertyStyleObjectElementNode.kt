package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for property-style object element.
 */
class PropertyStyleObjectElementNode private constructor(parser: LexemParser, val key: ParserNode,
        val value: ParenthesisExpressionNode) : ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(key)
        append(value)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("key", key)
        printer.addField("value", value)
    }

    companion object {

        /**
         * Parses a property-style object element.
         */
        fun parse(parser: LexemParser): PropertyStyleObjectElementNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PropertyStyleObjectElementNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val key = Commons.parseDynamicIdentifier(parser) ?: return null
            val value = ParenthesisExpressionNode.parse(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.PropertyStyleObjectElementWithoutExpressionAfterName,
                    "An parenthesized expression was expected after the name of the property.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding a value here e.g. '(value)'")
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the name of the property")
                }
            }

            val result = PropertyStyleObjectElementNode(parser, key, value)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
