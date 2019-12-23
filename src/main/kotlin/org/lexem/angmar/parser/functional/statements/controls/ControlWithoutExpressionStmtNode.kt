package org.lexem.angmar.parser.functional.statements.controls

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.controls.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for control statements without a expression.
 */
internal class ControlWithoutExpressionStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var tag: IdentifierNode? = null
    lateinit var keyword: String

    override fun toString() = StringBuilder().apply {
        append(keyword)
        if (tag != null) {
            append(GlobalCommons.tagPrefix)
            append(tag)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("keyword", keyword)
        result.add("tag", tag?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            ControlWithoutExpressionStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        const val exitKeyword = "exit"
        const val nextKeyword = "next"
        const val redoKeyword = "redo"
        const val restartKeyword = "restart"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a control statement without a expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, keyword: String): ControlWithoutExpressionStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ControlWithoutExpressionStmtNode(parser, parent)
            result.keyword = keyword

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            // tag
            let {
                val initTagCursor = parser.reader.saveCursor()

                if (!parser.readText(GlobalCommons.tagPrefix)) {
                    return@let
                }

                result.tag = IdentifierNode.parse(parser, result)
                if (result.tag == null) {
                    initTagCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
