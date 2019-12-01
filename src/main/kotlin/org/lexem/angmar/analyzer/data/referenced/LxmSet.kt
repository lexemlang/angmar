package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*

/**
 * The Lexem value of the Set type.
 */
internal class LxmSet(val oldSet: LxmSet?) : LexemReferenced {
    private var isConstant = false
    private var values = mutableMapOf<Int, MutableList<LxmSetProperty>>()

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new value to the set.
     */
    fun addValue(memory: LexemMemory, value: LexemPrimitive) {
        // Prevent modifications if the object is constant.
        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore cannot be modified") {}
        }

        val valueHash = value.getHashCode(memory)
        val currentProperty = getOwnPropertyDescriptorInCurrent(value, valueHash)
        val lastProperty = oldSet?.getOwnPropertyDescriptor(value, valueHash)

        when {
            // No property
            currentProperty == null && lastProperty == null -> {
                val property = LxmSetProperty(isRemoved = false)
                values.putIfAbsent(valueHash, mutableListOf())
                values[valueHash]!!.add(property)
                property.replaceValue(memory, value)
            }

            // Current property
            currentProperty != null -> {
                currentProperty.replaceValue(memory, value)
                currentProperty.isRemoved = false
            }

            // Property in past version of the object
            lastProperty != null -> {
                values.putIfAbsent(valueHash, mutableListOf())
                values[valueHash]!!.add(lastProperty.clone(memory, isRemoved = false))
            }
        }
    }

    /**
     * Returns whether the set contains a property or not.
     */
    fun containsValue(memory: LexemMemory, value: LexemPrimitive): Boolean {
        val descriptor = getOwnPropertyDescriptor(value, value.getHashCode(memory))
        return descriptor != null && !descriptor.isRemoved
    }

    /**
     * Removes a property.
     */
    fun removeProperty(memory: LexemMemory, value: LexemPrimitive) {
        // Prevent modifications if the object is constant.
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore it cannot be modified") {}
        }

        val valueHash = value.getHashCode(memory)
        val currentProperty = getOwnPropertyDescriptorInCurrent(value, valueHash)
        val lastProperty = oldSet?.getOwnPropertyDescriptor(value, valueHash)

        when {
            // Current property
            currentProperty != null -> {
                if (lastProperty != null) {
                    // Set property to remove.
                    currentProperty.replaceValue(memory, LxmNil)
                    currentProperty.isRemoved = true
                } else {
                    // Remove property.
                    values[valueHash]!!.remove(currentProperty)
                    currentProperty.replaceValue(memory, LxmNil)
                }
            }

            // Property in past version of the object
            lastProperty != null -> {
                // Set property to remove.
                val property = lastProperty.clone(memory, isRemoved = true)
                values.putIfAbsent(valueHash, mutableListOf())
                values[valueHash]!!.add(property)
                property.replaceValue(memory, LxmNil)
            }
        }
    }

    /**
     * Makes the set constant.
     */
    fun makeConstant(memory: LexemMemory) {
        isConstant = true
    }

    /**
     * Gets the property descriptor of the specified property in the current set.
     */
    private fun getOwnPropertyDescriptorInCurrent(value: LexemPrimitive, hash: Int): LxmSetProperty? {
        val list = values[hash] ?: return null

        for (i in list) {
            if (RelationalFunctions.identityEquals(value, i.value)) {
                return i
            }
        }

        return null
    }

    /**
     * Gets the property descriptor of the specified property.
     */
    private fun getOwnPropertyDescriptor(value: LexemPrimitive, hash: Int): LxmSetProperty? =
            getOwnPropertyDescriptorInCurrent(value, hash) ?: oldSet?.getOwnPropertyDescriptor(value, hash)

    /**
     * Gets all value of the set.
     */
    fun getAllValues(): Map<Int, List<LxmSetProperty>> {
        val result = mutableMapOf<Int, MutableList<LxmSetProperty>>()

        var currentObject: LxmSet? = this
        while (currentObject != null) {
            for (propList in currentObject.values) {
                val list = result[propList.key] ?: mutableListOf()
                result.putIfAbsent(propList.key, list)

                for (prop in propList.value) {
                    if (list.find { RelationalFunctions.identityEquals(it.value, prop.value) } == null) {
                        list.add(prop)
                    }
                }
            }

            currentObject = currentObject.oldSet
        }

        return result.mapValues { it.value.filter { !it.isRemoved } }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override val isImmutable: Boolean
        get() = isConstant

    override fun clone() = if (isImmutable) {
        this
    } else {
        LxmSet(this)
    }

    override fun memoryDealloc(memory: LexemMemory) {
        for (i in this.values) {
            for (j in i.value) {
                val reference = j.value
                if (reference is LxmReference) {
                    reference.decreaseReferences(memory)
                }
            }
        }

        this.values.clear()
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        for ((_, list) in this.values) {
            for (property in list) {
                property.value.spatialGarbageCollect(memory)
            }
        }
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, SetType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: LexemMemory) = LxmString.SetToString

    override fun toString() = StringBuilder().apply {
        append(SetNode.macroName)

        if (isConstant) {
            append(ListNode.constantToken)
        }

        append(ListNode.startToken)

        val properties = getAllValues().flatMap { it.value }
        val text = properties.asSequence().take(4).joinToString(", ") {
            it.value.toString()
        }

        append(text)
        append(ListNode.endToken)

        if (properties.size > 4) {
            append(" and ${properties.size - 4} more")
        }
    }.toString()

    // STATIC -----------------------------------------------------------------

    /**
     * A property of [LxmSet]s.
     */
    class LxmSetProperty(var isRemoved: Boolean) {
        var value: LexemPrimitive = LxmNil

        // METHODS ------------------------------------------------------------

        /**
         * Clones the object property only if it is not constant.
         */
        fun clone(memory: LexemMemory, isRemoved: Boolean? = null): LxmSetProperty {
            val property = LxmSetProperty(isRemoved = isRemoved ?: this.isRemoved)

            property.replaceValue(memory, value)

            return property
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(memory: LexemMemory, newValue: LexemPrimitive) {
            // Keep this to replace the elements before possibly remove the references.
            val oldValue = value
            value = newValue
            memory.replacePrimitives(oldValue, newValue)
        }
    }

    companion object {
        val Empty = LxmSet(null)

        init {
            Empty.isConstant = true
        }
    }
}
