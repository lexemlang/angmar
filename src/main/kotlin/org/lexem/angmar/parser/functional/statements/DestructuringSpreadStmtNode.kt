package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for spread element of destructuring.
 */
class DestructuringSpreadStmtNode private constructor(parser: LexemParser, val identifier: IdentifierNode) :
        ParserNode(parser) {
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        append(spreadToken)
        if (isConstant) {
            append(constantToken)
        }
        append(identifier)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("identifier", identifier)
        printer.addField("isConstant", isConstant)
    }

    companion object {
        const val spreadToken = GlobalCommons.spreadOperator
        const val constantToken = GlobalCommons.constantToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses for a spread element of destructuring.
         */
        fun parse(parser: LexemParser): DestructuringSpreadStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), DestructuringSpreadStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(spreadToken)) {
                return null
            }

            val isConstant = parser.readText(constantToken)

            val identifier = IdentifierNode.parse(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.DestructuringSpreadStatementWithoutIdentifierAfterSpreadOperator,
                    "An identifier was expected after spread operator '$spreadToken'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an identifier here here")
                }
            }

            val result = DestructuringSpreadStmtNode(parser, identifier)
            result.isConstant = isConstant

            return parser.finalizeNode(result, initCursor)
        }
    }
}
