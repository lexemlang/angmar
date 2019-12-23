package org.lexem.angmar.parser.descriptive

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Parser for accesses expression for data capturing.
 */
internal class DataCapturingAccessLexemeNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var element: IdentifierNode
    var modifiers = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(element)
        modifiers.forEach { append(it) }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("element", element.toTree())
        result.add("modifiers", SerializationUtils.listToTest(modifiers))

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            DataCapturingAccessLexemeCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses an access expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode): DataCapturingAccessLexemeNode? {
            val initCursor = parser.reader.saveCursor()
            val result = DataCapturingAccessLexemeNode(parser, parent)

            result.element = IdentifierNode.parse(parser, result) ?: return null

            while (true) {
                result.modifiers.add(AccessExplicitMemberNode.parse(parser, result) ?: IndexerNode.parse(parser, result)
                ?: FunctionCallNode.parse(parser, result) ?: break)
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
