package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for destructuring.
 */
class DestructuringStmtNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var alias: IdentifierNode? = null
    val elements = mutableListOf<DestructuringElementStmtNode>()
    var spread: DestructuringSpreadStmtNode? = null

    override fun toString() = StringBuilder().apply {
        if (alias != null) {
            append(alias)
        }
        append(startToken)
        append(elements.joinToString("$elementSeparator "))
        if (spread != null) {
            append("$elementSeparator ")
            append(spread)
        }
        append(endToken)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("alias", alias)
        printer.addField("elements", elements)
        printer.addField("spread", spread)
    }

    companion object {
        const val startToken = "("
        const val elementSeparator = GlobalCommons.elementSeparator
        const val endToken = ")"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a destructuring.
         */
        fun parse(parser: LexemParser): DestructuringStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), DestructuringStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = DestructuringStmtNode(parser)

            // alias.
            result.alias = IdentifierNode.parse(parser)

            if (result.alias != null) {
                WhitespaceNode.parse(parser)

                if (!parser.readText(elementSeparator)) {
                    initCursor.restore()
                    return null
                }

                WhitespaceNode.parse(parser)
            }

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            var element = DestructuringElementStmtNode.parse(parser)
            if (element != null) {
                result.elements.add(element)

                while (true) {
                    val initLoopCursor = parser.reader.saveCursor()

                    WhitespaceNode.parse(parser)

                    if (!parser.readText(elementSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)

                    element = DestructuringElementStmtNode.parse(parser)
                    if (element == null) {
                        initLoopCursor.restore()
                        break
                    }

                    result.elements.add(element)
                }
            }

            // spread.
            let {
                val initSpreadCursor = parser.reader.saveCursor()

                if (result.elements.isNotEmpty()) {
                    WhitespaceNode.parse(parser)

                    if (!parser.readText(elementSeparator)) {
                        initSpreadCursor.restore()
                        return@let
                    }

                    WhitespaceNode.parse(parser)
                }

                result.spread = DestructuringSpreadStmtNode.parse(parser)
                if (result.spread == null) {
                    initSpreadCursor.restore()
                }
            }

            // Trailing comma.
            let {
                val initTrailingCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!parser.readText(elementSeparator)) {
                    initTrailingCursor.restore()
                    return@let
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.DestructuringStatementWithoutEndToken,
                        "The close parenthesis was expected '$endToken'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the close parenthesis '$endToken' here")
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
