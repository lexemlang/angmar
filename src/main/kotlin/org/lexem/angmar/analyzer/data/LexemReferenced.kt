package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * The common part of every referenced value in lexem.
 */
internal abstract class LexemReferenced : LexemMemoryValue {
    /**
     * Holds a reference to the node that it belongs to.
     */
    var bigNode: BigNode

    /**
     * Holds the position of this object in the memory as a reference.
     */
    private val reference: LxmReference

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Constructor to create a new one.
     */
    constructor(memory: LexemMemory) {
        bigNode = memory.lastNode
        // IMPORTANT: bigNode must be set before calling the memory add because it checks
        // that property.
        reference = memory.add(this)
    }

    /**
     * Constructor to memory shift cloning or not.
     */
    protected constructor(memory: LexemMemory, oldVersion: LexemReferenced) {
        bigNode = memory.lastNode
        reference = oldVersion.reference

        // Check that a memoryShift is not called over the same bigNode.
        if (oldVersion.bigNode == memory.lastNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ValueShiftOverSameBigNode,
                    "Cannot shift a value in the same bigNode") {}
        }
    }


    // METHODS ----------------------------------------------------------------

    /**
     * Indicates whether the value is an immutable view of the memory value or can be modified.
     */
    fun isMemoryImmutable(memory: LexemMemory) = bigNode != memory.lastNode

    /**
     * Gets the reference of this value.
     */
    override fun getPrimitive() = reference

    /**
     * Clones the value in the memory.
     */
    abstract fun memoryShift(memory: LexemMemory): LexemReferenced

    /**
     * Clears the memory value.
     */
    abstract fun memoryDealloc(memory: LexemMemory)
}
