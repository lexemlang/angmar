package org.lexem.angmar.parser.functional.statements.controls

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.controls.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for control statements without a expression.
 */
internal class ControlWithoutExpressionStmtNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var keyword: String
    var tag: IdentifierNode? = null

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

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ControlWithoutExpressionStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val exitKeyword = "exit"
        const val nextKeyword = "next"
        const val redoKeyword = "redo"
        const val restartKeyword = "restart"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a control statement without a expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int,
                keyword: String): ControlWithoutExpressionStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ControlWithoutExpressionStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = ControlWithoutExpressionStmtNode(parser, parent, parentSignal)
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

                result.tag = IdentifierNode.parse(parser, result, ControlWithoutExpressionStmtAnalyzer.signalEndTag)
                if (result.tag == null) {
                    initTagCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
