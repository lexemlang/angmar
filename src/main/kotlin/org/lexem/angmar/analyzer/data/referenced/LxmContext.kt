package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*

/**
 * The Lexem value for the context.
 */
internal class LxmContext : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory)
    constructor(memory: LexemMemory, oldContext: LxmContext, toClone: Boolean) : super(memory, oldContext, toClone)
    constructor(higherContextReference: LxmReference, memory: LexemMemory) : super(higherContextReference, memory)

    // METHODS ----------------------------------------------------------------

    /**
     * Removes a property ignoring constants. Used for testing.
     */
    fun removePropertyIgnoringConstants(memory: LexemMemory, identifier: String) {
        val currentProperty = properties[identifier]
        val lastProperty = oldVersion?.getOwnPropertyDescriptor(memory, identifier)

        when {
            // Current property
            currentProperty != null -> {
                if (lastProperty != null) {
                    // Set property to remove.
                    currentProperty.isIterable = false
                    currentProperty.isRemoved = true
                    currentProperty.replaceValue(memory, LxmNil)
                } else {
                    // Remove property.
                    properties.remove(identifier)
                    currentProperty.replaceValue(memory, LxmNil)
                }
            }

            // Property in past version of the object
            lastProperty != null -> {
                // Set property to remove.
                val cloned = lastProperty.clone(isIterable = false, isRemoved = true)
                cloned.replaceValue(memory, LxmNil)
                properties[identifier] = cloned
            }
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun clone(memory: LexemMemory) = LxmContext(memory, this,
            toClone = (countOldVersions() ?: 0) >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[CONTEXT] ${super.toString()}"
}
