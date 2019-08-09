package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for elements of destructuring.
 */
class DestructuringElementStmtNode private constructor(parser: LexemParser, val alias: IdentifierNode) :
        ParserNode(parser) {
    var isConstant = false
    var original: IdentifierNode? = null

    override fun toString() = StringBuilder().apply {
        if (original != null) {
            append(original)
            append(' ')
            append(aliasToken)
            append(' ')
        }
        if (isConstant) {
            append(constantToken)
        }
        append(alias)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("isConstant", isConstant)
        printer.addOptionalField("original", original)
        printer.addField("alias", alias)
    }

    companion object {
        const val aliasToken = "as"
        const val constantToken = GlobalCommons.constantToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses for an element of destructuring.
         */
        fun parse(parser: LexemParser): DestructuringElementStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), DestructuringElementStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            // original
            var original: IdentifierNode? = null
            let {
                val initOriginalCursor = parser.reader.saveCursor()

                val ori = IdentifierNode.parse(parser) ?: return@let

                WhitespaceNode.parse(parser)

                if (!parser.readText(aliasToken)) {
                    initOriginalCursor.restore()
                    original = null
                    return@let
                }

                WhitespaceNode.parse(parser)

                original = ori
            }

            val isConstant = parser.readText(constantToken)

            val alias = IdentifierNode.parse(parser) ?: let {
                if (original != null) {
                    throw AngmarParserException(
                            AngmarParserExceptionType.DestructuringElementStatementWithoutIdentifierAfterAliasToken,
                            "An identifier was expected after the alias operator '$aliasToken'.") {
                        addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                            title(Consts.Logger.codeTitle)
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(parser.reader.readAllText(), null) {
                            title(Consts.Logger.hintTitle)
                            highlightCursorAt(parser.reader.currentPosition())
                            message("Try adding a whitespace followed by an identifier here")
                        }
                    }
                }

                initCursor.restore()
                return null
            }

            val result = DestructuringElementStmtNode(parser, alias)
            result.isConstant = isConstant
            result.original = original
            return parser.finalizeNode(result, initCursor)
        }
    }
}
