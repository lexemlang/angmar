package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*


/**
 * Parser for property-style object.
 */
class PropertyStyleObjectNode private constructor(parser: LexemParser, val block: PropertyStyleObjectBlockNode) :
        ParserNode(parser) {
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        append(startToken)

        if (isConstant) {
            append(constantToken)
        }

        append(block)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("isConstant", isConstant)
        printer.addField("block", block)
    }

    companion object {
        const val startToken = "@"
        const val constantToken = GlobalCommons.constantToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses a property-style object.
         */
        fun parse(parser: LexemParser): PropertyStyleObjectNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PropertyStyleObjectNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(startToken)) {
                return null
            }

            val isConstant = parser.readText(constantToken)
            val block = PropertyStyleObjectBlockNode.parse(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.PropertyStyleObjectWithoutStartToken,
                    "The open square bracket was expected '${PropertyStyleObjectBlockNode.startToken}'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding the open square bracket '${PropertyStyleObjectBlockNode.startToken}' here")
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message("Try removing the start token '$startToken'")
                }
            }

            val result = PropertyStyleObjectNode(parser, block)
            result.isConstant = isConstant
            return parser.finalizeNode(result, initCursor)
        }
    }
}
