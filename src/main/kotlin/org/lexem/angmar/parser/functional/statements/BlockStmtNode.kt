package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for block statements.
 */
class BlockStmtNode private constructor(parser: LexemParser) : ParserNode(parser) {
    var tag: IdentifierNode? = null
    val statements = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(startToken)
        if (tag != null) {
            append(tagPrefix)
            append(tag)
        }
        append('\n')
        append(statements.joinToString("\n    "))
        append('\n')
        append(endToken)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addOptionalField("tag", tag)
        printer.addField("statements", statements)
    }

    companion object {
        const val startToken = "{"
        const val endToken = "}"
        const val tagPrefix = GlobalCommons.tagPrefix

        // METHODS ------------------------------------------------------------

        /**
         * Parses a block statement.
         */
        fun parse(parser: LexemParser): BlockStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), BlockStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(startToken)) {
                return null
            }

            val result = BlockStmtNode(parser)

            // tag
            let {
                val initTagCursor = parser.reader.saveCursor()

                if (!parser.readText(tagPrefix)) {
                    return@let
                }

                result.tag = IdentifierNode.parse(parser)
                if (result.tag == null) {
                    initTagCursor.restore()
                }
            }

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val statement = StatementCommons.parseAnyStatement(parser)
                if (statement == null) {
                    initLoopCursor.restore()
                    break
                }

                result.statements.add(statement)
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.BlockStatementWithoutEndToken,
                        "The close bracket was expected '$endToken' to finish the block statement.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the close bracket '$endToken' here")
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
