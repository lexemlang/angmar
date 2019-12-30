package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.*

/**
 * The Lexem value of the filter type.
 */
internal class LxmFilter : LxmFunction {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, node: CompiledNode, name: String, context: LxmContext) : super(memory, node,
            context) {
        this.name = name
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(bigNode: BigNode): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(bigNode, toWrite = false)
        return context.getPropertyValue(FilterType.TypeName) as LxmReference
    }

    override fun toLexemString(bigNode: BigNode): LxmString {
        var source = node.parser.reader.getSource()
        val from = node.from.lineColumn()

        if (source.isBlank()) {
            source = "??"
        }

        val name = "[Filter $name at $source:${from.first}:${from.second}]"
        return LxmString.from(name)
    }

    override fun toString() = "[Filter] ${node.parser.reader.getSource()}::$name"
}
