package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for key-value pairs of map literals.
 */
class MapElementNode private constructor(parser: LexemParser, val key: ParserNode, val value: ParserNode) :
        ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(key)
        append(keyValueSeparator)
        append(' ')
        append(value)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("key", key)
        printer.addField("value", value)
    }

    companion object {
        const val keyValueSeparator = GlobalCommons.relationalToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses a key-value pair of map literal.
         */
        fun parse(parser: LexemParser): MapElementNode? {
            parser.fromBuffer(parser.reader.currentPosition(), MapElementNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val key = ExpressionsCommons.parseExpression(parser) ?: return null

            WhitespaceNode.parse(parser)

            if (!parser.readText(keyValueSeparator)) {
                throw AngmarParserException(AngmarParserExceptionType.MapElementWithoutRelationalSeparatorAfterKey,
                        "The relational separator '$keyValueSeparator' was expected after the key.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the relational separator '$keyValueSeparator' here")
                    }
                }
            }

            WhitespaceNode.parse(parser)

            val value = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.MapElementWithoutExpressionAfterRelationalSeparator,
                    "An expression acting as value was expected after the relational separator '$keyValueSeparator'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an expression here")
                }
            }

            val result = MapElementNode(parser, key, value)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
