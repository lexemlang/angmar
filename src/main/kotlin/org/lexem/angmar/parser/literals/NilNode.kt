package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for nil/null values.
 */
internal class NilNode private constructor(parser: LexemParser, parent: ParserNode) : ParserNode(parser, parent) {
    override fun toString() = nilLiteral

    override fun compile(parent: CompiledNode, parentSignal: Int) = ConstantCompiled(parent, parentSignal, this, LxmNil)

    companion object {
        const val nilLiteral = "nil"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a nil value.
         */
        fun parse(parser: LexemParser, parent: ParserNode): NilNode? {
            val initCursor = parser.reader.saveCursor()
            val result = if (Commons.parseKeyword(parser, nilLiteral)) {
                NilNode(parser, parent)
            } else {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
