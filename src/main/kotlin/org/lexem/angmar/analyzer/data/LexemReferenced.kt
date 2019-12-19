package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import java.util.*

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

    /**
     * The old version of this value.
     */
    val oldVersion: LexemReferenced?

    // CONSTRUCTORS -----------------------------------------------------------

    /**
     * Constructor to create a new one.
     */
    constructor(memory: LexemMemory) {
        bigNode = memory.lastNode
        reference = memory.add(this)
        oldVersion = null
    }

    /**
     * Constructor to memory shift cloning or not.
     */
    protected constructor(memory: LexemMemory, oldVersion: LexemReferenced, toClone: Boolean) {
        bigNode = memory.lastNode
        reference = oldVersion.reference
        this.oldVersion = if (toClone) {
            null
        } else {
            oldVersion
        }

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
     * Counts the number of old versions of this value.
     */
    fun countOldVersions(): Int {
        var count = 1

        var version = oldVersion
        while (version != null) {
            count += 1
            version = version.oldVersion
        }

        return count
    }

    /**
     * Counts the number of old versions of this value.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getListOfVersions(): LinkedList<T> {
        val list = LinkedList<T>()

        var version: LexemReferenced? = this
        while (version != null) {
            list.addLast(version as T)

            version = version.oldVersion
        }

        return list
    }

    /**
     * Gets the reference of this value.
     */
    override fun getPrimitive() = reference

    /**
     * Shifts the value in the memory.
     */
    abstract fun memoryShift(memory: LexemMemory): LexemReferenced

    /**
     * Clears the memory value.
     */
    abstract fun memoryDealloc(memory: LexemMemory)
}
