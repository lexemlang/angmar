package org.lexem.angmar.parser.functional.expressions.modifiers

import org.lexem.angmar.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for access explicit members i.e. element.access.
 */
class AccessExplicitMemberNode private constructor(parser: LexemParser, val identifier: IdentifierNode) :
        ParserNode(parser) {

    override fun toString() = "$accessCharacter$identifier"

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("identifier", identifier)
    }

    companion object {
        const val accessCharacter = "."

        // METHODS ------------------------------------------------------------

        /**
         * Parses an access expression.
         */
        fun parse(parser: LexemParser): AccessExplicitMemberNode? {
            parser.fromBuffer(parser.reader.currentPosition(), AccessExplicitMemberNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            if (!parser.readText(accessCharacter)) {
                // It is not an error because statements can end with a '.'
                return null
            }

            val id = IdentifierNode.parse(parser)
            if (id == null) {
                initCursor.restore()
                return null
            }

            val result = AccessExplicitMemberNode(parser, id)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
