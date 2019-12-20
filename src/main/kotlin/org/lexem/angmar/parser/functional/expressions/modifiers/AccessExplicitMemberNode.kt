package org.lexem.angmar.parser.functional.expressions.modifiers

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for access explicit members i.e. element.access.
 */
internal class AccessExplicitMemberNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var identifier: IdentifierNode

    override fun toString() = "$accessToken$identifier"

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("identifier", identifier.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            AccessExplicitMemberAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val accessToken = "."

        // METHODS ------------------------------------------------------------

        /**
         * Parses an access expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): AccessExplicitMemberNode? {
            val initCursor = parser.reader.saveCursor()
            val result = AccessExplicitMemberNode(parser, parent, parentSignal)

            if (!parser.readText(accessToken)) {
                // It is not an error because statements can end with a '.'
                return null
            }

            val id = IdentifierNode.parse(parser, result, AccessExplicitMemberAnalyzer.signalEndIdentifier)
            if (id == null) {
                initCursor.restore()
                return null
            }

            result.identifier = id

            return parser.finalizeNode(result, initCursor)
        }
    }
}
