package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
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
    constructor(memory: LexemMemory) {
        bigNode = memory.lastNode
        reference = memory.add(this)
    }

    /**
     * Constructor to clone.
     */
    protected constructor(bigNode: BigNode, oldVersion: LexemReferenced) {
        // Check that a clone is not called over the same bigNode.
        if (oldVersion.bigNode == bigNode) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.ValueShiftOverSameBigNode,
                    "Cannot shift a value in the same bigNode") {}
        }

        this.bigNode = bigNode
        reference = oldVersion.reference
    }


    // METHODS ----------------------------------------------------------------

    /**
     * Indicates whether the value is an immutable view of the memory value or can be modified.
     */
    fun isMemoryImmutable(memory: LexemMemory) = bigNode != memory.lastNode

    /**
     * Gets the prototype of the value.
     */
    fun getPrototype() = getPrototype(bigNode)

    /**
     * Gets the prototype of the value as a [LxmObject].
     */
    fun getPrototypeAsObject(toWrite: Boolean) = getPrototypeAsObject(bigNode, toWrite)

    /**
     * Gets the reference of this value.
     */
    override fun getPrimitive() = reference

    /**
     * Clones the value in the memory.
     */
    abstract fun memoryClone(bigNode: BigNode): LexemReferenced

    /**
     * Clears the memory value.
     */
    abstract fun memoryDealloc()
}
