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
    val bigNodeId: Int

    // IMPORTANT: Keep this under bigNode property to prevent errors with the memory
    // add because it checks the bigNode property.
    /**
     * Holds the position of this object in the memory as a reference.
     */
    private val reference: LxmReference

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Constructor to create a new one.
     */
    constructor(memory: IMemory) {
        bigNodeId = memory.getBigNodeId()
        reference = memory.add(this)
    }

    /**
     * Constructor to clone.
     */
    protected constructor(memory: IMemory, oldVersion: LexemReferenced) {
        // Check that a clone is not called over the same bigNode.
        if (oldVersion.bigNodeId == memory.getBigNodeId()) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ValueShiftOverSameBigNode,
                    "Cannot clone a value in the same bigNode.") {}
        }

        this.bigNodeId = memory.getBigNodeId()
        reference = oldVersion.reference
    }


    // METHODS ----------------------------------------------------------------

    /**
     * Indicates whether the value is an immutable view of the memory value or can be modified.
     */
    fun isMemoryImmutable(memory: IMemory) = bigNodeId != memory.getBigNodeId()

    /**
     * Gets the reference of this value.
     */
    override fun getPrimitive() = reference

    /**
     * Clones the value in the memory.
     */
    abstract fun memoryClone(memory: IMemory): LexemReferenced

    /**
     * Clears the memory value.
     */
    abstract fun memoryDealloc(memory: IMemory)
}
