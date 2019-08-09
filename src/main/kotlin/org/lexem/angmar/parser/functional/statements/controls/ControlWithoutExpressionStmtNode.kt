package org.lexem.angmar.parser.functional.statements.controls

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for control statements without a expression.
 */
class ControlWithoutExpressionStmtNode private constructor(parser: LexemParser, val keyword: String) :
        ParserNode(parser) {
    var tag: IdentifierNode? = null

    override fun toString() = StringBuilder().apply {
        append(keyword)
        if (tag != null) {
            append(GlobalCommons.tagPrefix)
            append(tag)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("keyword", keyword)
        printer.addOptionalField("tag", tag)
    }

    companion object {
        const val nextKeyword = "next"
        const val redoKeyword = "redo"
        const val restartKeyword = "restart"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a control statement without a expression.
         */
        fun parse(parser: LexemParser, keyword: String): ControlWithoutExpressionStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ControlWithoutExpressionStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ControlWithoutExpressionStmtNode(parser, keyword)

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            // tag
            let {
                val initTagCursor = parser.reader.saveCursor()

                if (!parser.readText(GlobalCommons.tagPrefix)) {
                    return@let
                }

                result.tag = IdentifierNode.parse(parser)
                if (result.tag == null) {
                    initTagCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
