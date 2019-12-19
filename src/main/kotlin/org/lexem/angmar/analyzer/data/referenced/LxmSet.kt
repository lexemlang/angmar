package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*

/**
 * The Lexem value of the Set type.
 */
internal class LxmSet : LexemReferenced {
    var isConstant = false
        private set
    private var values: MutableMap<Int, MutableList<LxmSetProperty>>

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory) {
        values = mutableMapOf()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmSet, toClone: Boolean) : super(memory, oldVersion,
            toClone) {
        isConstant = oldVersion.isConstant
        values = if (toClone) {
            val map = oldVersion.getAllValues()

            for ((key, list) in map) {
                map[key] = list.mapTo(mutableListOf()) { it.clone(memory) }
            }

            map
        } else {
            mutableMapOf()
        }
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new value to the set.
     */
    fun addValue(memory: LexemMemory, value: LexemMemoryValue) {
        // Prevent modifications if the object is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The set is immutable therefore cannot be modified") {}
        }

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore cannot be modified") {}
        }

        val valuePrimitive = value.getPrimitive()
        val valueHash = valuePrimitive.getHashCode(memory)
        val currentProperty = getOwnPropertyDescriptorInCurrent(valuePrimitive, valueHash)
        val lastProperty = (oldVersion as? LxmSet)?.getOwnPropertyDescriptor(valuePrimitive, valueHash)

        when {
            // No property
            currentProperty == null && lastProperty == null -> {
                val property = LxmSetProperty(isRemoved = false)
                values.putIfAbsent(valueHash, mutableListOf())
                values[valueHash]!!.add(property)
                property.replaceValue(memory, valuePrimitive)
            }

            // Current property
            currentProperty != null -> {
                currentProperty.replaceValue(memory, valuePrimitive)
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
    fun containsValue(memory: LexemMemory, value: LexemMemoryValue): Boolean {
        val valuePrimitive = value.getPrimitive()
        val descriptor = getOwnPropertyDescriptor(valuePrimitive, valuePrimitive.getHashCode(memory))
        return descriptor != null && !descriptor.isRemoved
    }

    /**
     * Removes a value.
     */
    fun removeValue(memory: LexemMemory, value: LexemMemoryValue) {
        // Prevent modifications if the object is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The set is immutable therefore cannot be modified") {}
        }

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore it cannot be modified") {}
        }

        val valuePrimitive = value.getPrimitive()
        val valueHash = valuePrimitive.getHashCode(memory)
        val currentProperty = getOwnPropertyDescriptorInCurrent(valuePrimitive, valueHash)
        val lastProperty = (oldVersion as? LxmSet)?.getOwnPropertyDescriptor(valuePrimitive, valueHash)

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
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The set is immutable therefore cannot be modified") {}
        }

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
            getOwnPropertyDescriptorInCurrent(value, hash) ?: (oldVersion as? LxmSet)?.getOwnPropertyDescriptor(value,
                    hash)

    /**
     * Gets the size of the set.
     */
    fun getSize(): Int {
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

            currentObject = currentObject.oldVersion as? LxmSet
        }

        return result.map { it.value.size }.sum()
    }

    /**
     * Gets all value of the set.
     */
    fun getAllValues(): MutableMap<Int, MutableList<LxmSetProperty>> {
        val versions = getListOfVersions<LxmSet>()

        // Iterate to get a list of versions.
        val result = mutableMapOf<Int, MutableList<LxmSetProperty>>()

        while (versions.isNotEmpty()) {
            val element = versions.removeLast()
            result.putAll(element.values)
        }

        return result
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryShift(memory: LexemMemory) = if (isConstant) {
        this
    } else {
        LxmSet(memory, this, toClone = countOldVersions() >= Consts.Memory.maxVersionCountToFullyCopyAValue)
    }

    override fun memoryDealloc(memory: LexemMemory) {
        for (i in values) {
            for (j in i.value) {
                val reference = j.value
                if (reference is LxmReference) {
                    reference.decreaseReferences(memory)
                }
            }
        }

        if (!isConstant) {
            values.clear()
        }
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        for ((_, list) in getAllValues()) {
            for (property in list) {
                property.value.spatialGarbageCollect(memory, gcFifo)
            }
        }
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
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
}
