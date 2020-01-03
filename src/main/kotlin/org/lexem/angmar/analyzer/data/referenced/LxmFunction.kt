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
    var contextReference: LxmReference?
        private set
    val internalFunction: ((LexemAnalyzer, LxmArguments, LxmFunction, Int) -> Boolean)?

    private val isInternalFunction get() = internalFunction != null

    val parserNode get() = node
    private val parentContextReference get() = contextReference

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Builds a custom function.
     */
    constructor(memory: IMemory, node: CompiledNode, context: LxmContext) : super(memory) {
        this.node = node
        this.contextReference = context.getPrimitive()
        this.internalFunction = null

        contextReference!!.increaseReferences(memory)
    }

    /**
     * Builds a built-in function.
     */
    constructor(memory: IMemory,
            internalFunction: (analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) -> Boolean) : super(
            memory) {
        this.node = InternalFunctionCallCompiled
        this.contextReference = null
        this.internalFunction = internalFunction
    }

    /**
     * Builds a built-in function with context.
     */
    constructor(memory: IMemory, context: LxmContext,
            internalFunction: (analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) -> Boolean) : super(
            memory) {
        this.node = InternalFunctionCallCompiled
        this.contextReference = context.getPrimitive()
        this.internalFunction = internalFunction

        contextReference!!.increaseReferences(memory)
    }

    /**
     * Builds a built-in function with some arguments instead of a context.
     * Used for wrap functions.
     */
    constructor(memory: IMemory, arguments: LxmArguments,
            internalFunction: (analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction, signal: Int) -> Boolean) : super(
            memory) {
        this.node = InternalFunctionCallCompiled
        this.contextReference = arguments.getPrimitive()
        this.internalFunction = internalFunction

        contextReference!!.increaseReferences(memory)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the dereferenced parent context.
     */
    fun getParentContext(memory: IMemory, toWrite: Boolean) =
            parentContextReference?.dereferenceAs<LxmContext>(memory, toWrite)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(memory: IMemory) = this

    override fun memoryDealloc(memory: IMemory) {
        contextReference?.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) {
        contextReference?.spatialGarbageCollect(gcFifo)
    }

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, FunctionType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: IMemory) = if (isInternalFunction) {
        LxmString.InternalFunctionToString
    } else {
        var source = node.parser.reader.getSource()
        val from = node.parserNode.from.lineColumn()

        if (source.isBlank()) {
            source = "??"
        }

        val name = "[Function $name at $source:${from.first}:${from.second}]"
        LxmString.from(name)
    }

    override fun toString() = "[Function] ${node.parser.reader.getSource()}::$name - Context: $contextReference"
}
