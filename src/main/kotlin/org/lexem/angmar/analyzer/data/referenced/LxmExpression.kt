package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.*

/**
 * The Lexem value of the expression type.
 */
internal class LxmExpression : LxmFunction {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, node: ParserNode, name: String, contextReference: LxmReference) : super(memory,
            node, contextReference) {
        this.name = name
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, ExpressionType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: LexemMemory): LxmString {
        var source = node.parser.reader.getSource()
        val from = node.from.lineColumn()

        if (source.isBlank()) {
            source = "??"
        }

        val name = "[Expression $name at $source:${from.first}:${from.second}]"
        return LxmString.from(name)
    }

    override fun toString() = "[Expression] ${node.parser.reader.getSource()}::$name"
}
