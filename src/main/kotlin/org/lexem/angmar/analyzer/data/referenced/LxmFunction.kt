package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*

/**
 * The Lexem value of the function type.
 */
internal open class LxmFunction : LexemReferenced {
    var name: String = "<Anonymous function>"
    val node: CompiledNode
    val contextReference: LxmReference?
    val internalFunction: ((LexemAnalyzer, LxmArguments, LxmFunction, Int) -> Boolean)?

    val isInternalFunction get() = internalFunction != null

    val parserNode get() = node
    val parentContextReference get() = contextReference

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Builds a custom function.
     */
    constructor(memory: LexemMemory, node: CompiledNode, context: LxmContext) : super(memory) {
        this.node = node
        this.contextReference = context.getPrimitive()
        this.internalFunction = null

        contextReference.increaseReferences(memory)
    }

    /**
     * Builds a built-in function.
     */
    constructor(memory: LexemMemory,
            internalFunction: (analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) -> Boolean) : super(
            memory) {
        this.node = InternalFunctionCallCompiled
        this.contextReference = null
        this.internalFunction = internalFunction
    }

    /**
     * Builds a built-in function with context.
     */
    constructor(memory: LexemMemory, context: LxmContext,
            internalFunction: (analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) -> Boolean) : super(
            memory) {
        this.node = InternalFunctionCallCompiled
        this.contextReference = context.getPrimitive()
        this.internalFunction = internalFunction

        contextReference.increaseReferences(memory)
    }

    /**
     * Builds a built-in function with some arguments instead of a context.
     * Used for wrap functions.
     */
    constructor(memory: LexemMemory, arguments: LxmArguments,
            internalFunction: (analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) -> Boolean) : super(
            memory) {
        this.node = InternalFunctionCallCompiled
        this.contextReference = arguments.getPrimitive()
        this.internalFunction = internalFunction

        contextReference.increaseReferences(memory)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the dereferenced parent context.
     */
    fun getParentContext(memory: LexemMemory, toWrite: Boolean) =
            parentContextReference?.dereferenceAs<LxmContext>(memory, toWrite)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryShift(memory: LexemMemory) = this

    override fun memoryDealloc(memory: LexemMemory) {
        contextReference?.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        contextReference?.spatialGarbageCollect(memory, gcFifo)
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, FunctionType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: LexemMemory) = if (isInternalFunction) {
        LxmString.InternalFunctionToString
    } else {
        var source = node.parser.reader.getSource()
        val from = node.parserNode!!.from.lineColumn()

        if (source.isBlank()) {
            source = "??"
        }

        val name = "[Function $name at $source:${from.first}:${from.second}]"
        LxmString.from(name)
    }

    override fun toString() = "[Function] ${node.parser.reader.getSource()}::$name - Context: $contextReference"
}
