package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.*

/**
 * The lexem value of the function type.
 */
internal class LxmFunction : LexemReferenced, ExecutableValue {
    val node: ParserNode
    val contextReference: LxmReference

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, node: ParserNode, contextReference: LxmReference) {
        this.node = node
        this.contextReference = contextReference

        contextReference.increaseReferenceCount(memory)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override val isImmutable = true

    override val parserNode get() = node

    override val parentContext get() = contextReference

    override fun clone() = this

    override fun memoryDealloc(memory: LexemMemory) {
        contextReference.decreaseReferenceCount(memory)
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        contextReference.getCell(memory).spatialGarbageCollect(memory)
    }

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, FunctionType.TypeName)

    override fun toString() = "[Function]"
}
