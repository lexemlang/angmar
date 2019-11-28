package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.*

/**
 * The Lexem value of the function type.
 */
internal open class LxmFunction : LexemReferenced, ExecutableValue {
    var name: String = "??"
    val node: ParserNode
    val contextReference: LxmReference

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, node: ParserNode, contextReference: LxmReference) {
        this.node = node
        this.contextReference = contextReference

        contextReference.increaseReferences(memory)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override val isImmutable = true

    override val parserNode get() = node

    override val parentContext get() = contextReference

    override fun clone() = this

    override fun memoryDealloc(memory: LexemMemory) {
        contextReference.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        contextReference.spatialGarbageCollect(memory)
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, FunctionType.TypeName) as LxmReference
    }

    override fun toString() = "[Function] ${node.parser.reader.getSource()}::$name - Context: $contextReference"
}
