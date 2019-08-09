package org.lexem.angmar.parser.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for expression properties of function calls.
 */
class FunctionCallExpressionPropertiesNode private constructor(parser: LexemParser,
        val value: PropertyStyleObjectBlockNode) : ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(relationalToken)
        append(value)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("value", value)
    }

    companion object {
        const val relationalToken = GlobalCommons.relationalToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses an expression properties of function calls.
         */
        fun parse(parser: LexemParser): FunctionCallExpressionPropertiesNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionCallExpressionPropertiesNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(relationalToken)) {
                return null
            }

            val value = PropertyStyleObjectBlockNode.parse(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.FunctionCallExpressionPropertiesWithoutPropertyStyleBlockAfterRelationalToken,
                    "A property-style block was expected after the relational token '$relationalToken'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an empty property-style block here '${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.endToken}'")
                }
            }

            val result = FunctionCallExpressionPropertiesNode(parser, value)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
