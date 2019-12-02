package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * The Lexem value of the function type.
 */
internal open class LxmFunction : LexemReferenced, ExecutableValue {
    var name: String = "??"
    val node: ParserNode
    val contextReference: LxmReference?
    val internalFunction: ((LexemAnalyzer, LxmReference, LxmFunction, Int) -> Boolean)?

    val isInternalFunction get() = internalFunction != null

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Builds a custom function.
     */
    constructor(memory: LexemMemory, node: ParserNode, contextReference: LxmReference) {
        this.node = node
        this.contextReference = contextReference
        this.internalFunction = null

        contextReference.increaseReferences(memory)
    }

    /**
     * Builds a built-in function.
     */
    constructor(
            internalFunction: (analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) -> Boolean) {
        this.node = InternalFunctionCallNode
        this.contextReference = null
        this.internalFunction = internalFunction
    }

    /**
     * Builds a built-in function with context.
     */
    constructor(memory: LexemMemory, contextReference: LxmReference,
            internalFunction: (analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction, signal: Int) -> Boolean) {
        this.node = InternalFunctionCallNode
        this.contextReference = contextReference
        this.internalFunction = internalFunction

        contextReference.increaseReferences(memory)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override val isImmutable = true

    override val parserNode get() = node

    override val parentContext get() = contextReference

    override fun clone() = this

    override fun memoryDealloc(memory: LexemMemory) {
        contextReference?.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        contextReference?.spatialGarbageCollect(memory)
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, FunctionType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: LexemMemory) = if (isInternalFunction) {
        LxmString.InternalFunctionToString
    } else {
        var source = node.parser.reader.getSource()
        val from = node.from.lineColumn()

        if (source.isBlank()) {
            source = "??"
        }

        val name = "[Function $name at $source:${from.first}:${from.second}]"
        LxmString.from(name)
    }

    override fun toString() = "[Function] ${node.parser.reader.getSource()}::$name - Context: $contextReference"
}
