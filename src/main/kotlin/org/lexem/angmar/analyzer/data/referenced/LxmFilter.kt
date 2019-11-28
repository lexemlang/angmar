package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.*

/**
 * The Lexem value of the filter type.
 */
internal class LxmFilter : LxmFunction {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, node: ParserNode, name: String, contextReference: LxmReference) : super(memory,
            node, contextReference) {
        this.name = name
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, FilterType.TypeName) as LxmReference
    }

    override fun toString() = "[Filter] ${node.parser.reader.getSource()}::$name"
}
