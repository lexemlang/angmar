package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for key-value pairs of object literals.
 */
class ObjectElementNode private constructor(parser: LexemParser, val key: ParserNode, val value: ParserNode) :
        ParserNode(parser) {
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        if (isConstant) {
            append(constantToken)
        }
        append(key)
        append(keyValueSeparator)
        append(' ')
        append(value)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("key", key)
        printer.addField("value", value)
        printer.addField("isConstant", isConstant)
    }

    companion object {
        const val constantToken = GlobalCommons.constantToken
        const val keyValueSeparator = GlobalCommons.relationalToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses a key-value pair of object literal.
         */
        fun parse(parser: LexemParser): ObjectElementNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ObjectElementNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val isConstant = parser.readText(constantToken)
            val key = Commons.parseDynamicIdentifier(parser) ?: let {
                if (isConstant) {
                    throw AngmarParserException(AngmarParserExceptionType.ObjectElementWithoutKeyAfterConstantToken,
                            "A key was expected after the constant token '$constantToken'.") {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightSection(parser.reader.currentPosition() - 1)
                            message("Try removing the constant token here")
                        }
                    }
                }

                initCursor.restore()
                return@parse null
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(keyValueSeparator)) {
                throw AngmarParserException(AngmarParserExceptionType.ObjectElementWithoutRelationalOperatorAfterKey,
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
                    AngmarParserExceptionType.ObjectElementWithoutExpressionAfterRelationalOperator,
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

            val result = ObjectElementNode(parser, key, value)
            result.isConstant = isConstant

            return parser.finalizeNode(result, initCursor)
        }
    }
}
