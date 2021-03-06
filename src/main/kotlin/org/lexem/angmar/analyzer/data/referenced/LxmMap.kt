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
 * The Lexem value of the Map type.
 */
internal class LxmMap(val oldMap: LxmMap?) : LexemReferenced {
    private var isConstant = false
    private var properties = mutableMapOf<Int, MutableList<LxmMapProperty>>()

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a property.
     */
    fun getPropertyValue(memory: LexemMemory, key: LexemPrimitive): LexemPrimitive? {
        val property = getOwnPropertyDescriptor(key, key.getHashCode(memory)) ?: return null

        if (property.isRemoved) {
            return null
        }

        return property.value
    }

    /**
     * Gets the final value of a property.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedProperty(memory: LexemMemory, key: LexemPrimitive): T? =
            getPropertyValue(memory, key) as? T

    /**
     * Sets a new value to the property or creates a new property with the specified value.
     */
    fun setProperty(memory: LexemMemory, key: LexemPrimitive, value: LexemPrimitive) {
        // Prevent modifications if the object is constant.
        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore cannot be modified") {}
        }

        val keyHash = key.getHashCode(memory)
        val currentProperty = getOwnPropertyDescriptorInCurrent(key, keyHash)
        val lastProperty = oldMap?.getOwnPropertyDescriptor(key, keyHash)

        when {
            // No property
            currentProperty == null && lastProperty == null -> {
                val property = LxmMapProperty(isRemoved = false)
                property.replaceKey(memory, key)
                property.replaceValue(memory, value)

                properties.putIfAbsent(keyHash, mutableListOf())
                properties[keyHash]!!.add(property)
            }

            // Current property
            currentProperty != null -> {
                currentProperty.replaceValue(memory, value)
                currentProperty.isRemoved = false
            }

            // Property in past version of the object
            lastProperty != null -> {
                properties.putIfAbsent(keyHash, mutableListOf())
                properties[keyHash]!!.add(lastProperty.clone(memory, isRemoved = false))
            }
        }
    }

    /**
     * Returns whether the map contains a property or not.
     */
    fun containsProperty(memory: LexemMemory, key: LexemPrimitive): Boolean {
        val prop = getOwnPropertyDescriptor(key, key.getHashCode(memory))
        return prop != null && !prop.isRemoved
    }

    /**
     * Removes a property.
     */
    fun removeProperty(memory: LexemMemory, key: LexemPrimitive) {
        // Prevent modifications if the object is constant.
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore it cannot be modified") {}
        }

        val keyHash = key.getHashCode(memory)
        val currentProperty = getOwnPropertyDescriptorInCurrent(key, keyHash)
        val lastProperty = oldMap?.getOwnPropertyDescriptor(key, keyHash)

        when {
            // Current property
            currentProperty != null -> {
                if (lastProperty != null) {
                    // Set property to remove.
                    currentProperty.replaceValue(memory, LxmNil)
                    currentProperty.isRemoved = true
                } else {
                    // Remove property.
                    properties[keyHash]!!.remove(currentProperty)
                    currentProperty.replaceKey(memory, LxmNil)
                    currentProperty.replaceValue(memory, LxmNil)
                }
            }

            // Property in past version of the object
            lastProperty != null -> {
                // Set property to remove.
                val property = lastProperty.clone(memory, isRemoved = true)
                property.replaceValue(memory, LxmNil)
                properties.putIfAbsent(keyHash, mutableListOf())
                properties[keyHash]!!.add(property)
            }
        }
    }

    /**
     * Makes the map constant.
     */
    fun makeConstant() {
        isConstant = true
    }

    /**
     * Gets the property descriptor of the specified property in the current set.
     */
    private fun getOwnPropertyDescriptorInCurrent(key: LexemPrimitive, hash: Int): LxmMapProperty? {
        val list = properties[hash] ?: return null

        for (i in list) {
            if (RelationalFunctions.identityEquals(key, i.key)) {
                return i
            }
        }

        return null
    }

    /**
     * Gets the property descriptor of the specified property.
     */
    private fun getOwnPropertyDescriptor(key: LexemPrimitive, hash: Int): LxmMapProperty? =
            getOwnPropertyDescriptorInCurrent(key, hash) ?: oldMap?.getOwnPropertyDescriptor(key, hash)


    /**
     * Gets all properties of the map.
     */
    fun getAllProperties(): Map<Int, List<LxmMapProperty>> {
        val result = mutableMapOf<Int, MutableList<LxmMapProperty>>()

        var currentObject: LxmMap? = this
        while (currentObject != null) {
            for (propList in currentObject.properties) {
                val list = result[propList.key] ?: mutableListOf()
                result.putIfAbsent(propList.key, list)

                for (prop in propList.value) {
                    if (list.find { RelationalFunctions.identityEquals(it.key, prop.key) } == null) {
                        list.add(prop)
                    }
                }
            }

            currentObject = currentObject.oldMap
        }

        return result.mapValues { it.value.filter { !it.isRemoved } }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override val isImmutable: Boolean
        get() = isConstant

    override fun clone() = LxmMap(this)

    override fun memoryDealloc(memory: LexemMemory) {
        for (i in this.properties) {
            for (j in i.value) {
                val key = j.key
                if (key is LxmReference) {
                    key.decreaseReferences(memory)
                }

                val value = j.value
                if (value is LxmReference) {
                    value.decreaseReferences(memory)
                }
            }
        }

        this.properties.clear()
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        for ((_, list) in this.properties) {
            for (property in list) {
                property.key.spatialGarbageCollect(memory)
                property.value.spatialGarbageCollect(memory)
            }
        }
    }

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, MapType.TypeName)

    override fun toString() = StringBuilder().apply {
        append(MapNode.macroName)

        if (isConstant) {
            append(MapNode.constantToken)
        }

        append(MapNode.startToken)

        val properties = getAllProperties().flatMap { it.value }
        val text = properties.asSequence().take(4).joinToString("${MapNode.elementSeparator} ") {
            "${it.key}${MapElementNode.keyValueSeparator} ${it.value}"
        }

        append(text)
        append(MapNode.endToken)

        if (properties.size > 4) {
            append(" and ${properties.size - 4} more")
        }
    }.toString()

    // STATIC -----------------------------------------------------------------

    /**
     * A property of [LxmMap]s.
     */
    class LxmMapProperty(var isRemoved: Boolean) {
        var key: LexemPrimitive = LxmNil
        var value: LexemPrimitive = LxmNil

        // METHODS ------------------------------------------------------------

        /**
         * Clones the object property only if it is not constant.
         */
        fun clone(memory: LexemMemory, isRemoved: Boolean? = null): LxmMapProperty {
            val property = LxmMapProperty(isRemoved = isRemoved ?: this.isRemoved)

            property.replaceKey(memory, key)
            property.replaceValue(memory, value)

            return property
        }

        /**
         * Replaces the key handling the memory references.
         */
        fun replaceKey(memory: LexemMemory, newKey: LexemPrimitive) {
            memory.replacePrimitives(key, newKey)
            this.key = newKey
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(memory: LexemMemory, newValue: LexemPrimitive) {
            memory.replacePrimitives(value, newValue)
            this.value = newValue
        }
    }

    companion object {
        val Empty = LxmMap(null)

        init {
            Empty.isConstant = true
        }
    }
}
