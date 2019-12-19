package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*

/**
 * The Lexem value for the context.
 */
internal class LxmContext : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory)

    private constructor(memory: LexemMemory, oldContext: LxmContext, toClone: Boolean) : super(memory, oldContext,
            toClone)

    constructor(memory: LexemMemory, higherContextReference: LxmContext) : super(memory, higherContextReference)

    // METHODS ----------------------------------------------------------------

    /**
     * Removes a property ignoring constants. Used for testing.
     */
    fun removePropertyIgnoringConstants(memory: LexemMemory, identifier: String) {
        val currentProperty = properties[identifier]
        val lastProperty = (oldVersion as? LxmObject)?.getOwnPropertyDescriptor(memory, identifier)

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

    override fun getPrototype(memory: LexemMemory) = prototypeReference ?: let {
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        val type =
                context.getPropertyValue(memory, AnyType.TypeName)!!.dereference(memory, toWrite = false) as LxmObject

        return type.getPropertyValue(memory, AnalyzerCommons.Identifiers.Prototype) as LxmReference
    }

    override fun memoryShift(memory: LexemMemory) =
            LxmContext(memory, this, toClone = countOldVersions() >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[Context] ${super.toString()}"
}
