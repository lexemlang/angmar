package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*

/**
 * Parser for logical values.
 */
internal class LogicNode private constructor(parser: LexemParser, parent: ParserNode) : ParserNode(parser, parent) {
    var value: Boolean = false

    override fun toString() = if (value) {
        trueLiteral
    } else {
        falseLiteral
    }

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("value", value)

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            ConstantCompiled(parent, parentSignal, this, LxmLogic.from(value))

    companion object {
        const val trueLiteral = "true"
        const val falseLiteral = "false"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a logical value.
         */
        fun parse(parser: LexemParser, parent: ParserNode): LogicNode? {
            val initCursor = parser.reader.saveCursor()
            val result = when {
                Commons.parseKeyword(parser, trueLiteral) -> {
                    val res = LogicNode(parser, parent)
                    res.value = true
                    res
                }
                Commons.parseKeyword(parser, falseLiteral) -> {
                    LogicNode(parser, parent)
                }
                else -> return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
