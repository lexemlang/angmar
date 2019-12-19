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
 * The Lexem value of the Map type.
 */
internal class LxmMap : LexemReferenced {
    var isConstant = false
        private set
    private var properties: MutableMap<Int, MutableList<LxmMapProperty>>

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory) {
        properties = mutableMapOf()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmMap, toClone: Boolean) : super(memory, oldVersion,
            toClone) {
        isConstant = oldVersion.isConstant
        properties = if (toClone) {
            val map = oldVersion.getAllProperties()

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
     * Gets the value of a property.
     */
    fun getPropertyValue(memory: LexemMemory, key: LexemMemoryValue): LexemPrimitive? {
        val keyPrimitive = key.getPrimitive()
        val property = getOwnPropertyDescriptor(keyPrimitive, keyPrimitive.getHashCode(memory)) ?: return null

        if (property.isRemoved) {
            return null
        }

        return property.value
    }

    /**
     * Gets the final value of a property.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedProperty(memory: LexemMemory, key: LexemPrimitive,
            toWrite: Boolean): T? = getPropertyValue(memory, key)?.dereference(memory, toWrite) as? T

    /**
     * Sets a new value to the property or creates a new property with the specified value.
     */
    fun setProperty(memory: LexemMemory, key: LexemMemoryValue, value: LexemMemoryValue) {
        // Prevent modifications if the object is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The map is immutable therefore cannot be modified") {}
        }

        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore cannot be modified") {}
        }

        val keyPrimitive = key.getPrimitive()
        val valuePrimitive = value.getPrimitive()
        val keyHash = keyPrimitive.getHashCode(memory)
        val currentProperty = getOwnPropertyDescriptorInCurrent(keyPrimitive, keyHash)
        val lastProperty = (oldVersion as? LxmMap)?.getOwnPropertyDescriptor(keyPrimitive, keyHash)

        when {
            // No property
            currentProperty == null && lastProperty == null -> {
                val property = LxmMapProperty(isRemoved = false)
                properties.putIfAbsent(keyHash, mutableListOf())
                properties[keyHash]!!.add(property)
                property.replaceKey(memory, keyPrimitive)
                property.replaceValue(memory, valuePrimitive)
            }

            // Current property
            currentProperty != null -> {
                currentProperty.replaceValue(memory, valuePrimitive)
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
    fun containsProperty(memory: LexemMemory, key: LexemMemoryValue): Boolean {
        val keyPrimitive = key.getPrimitive()
        val prop = getOwnPropertyDescriptor(keyPrimitive, keyPrimitive.getHashCode(memory))
        return prop != null && !prop.isRemoved
    }

    /**
     * Removes a property.
     */
    fun removeProperty(memory: LexemMemory, key: LexemMemoryValue) {
        // Prevent modifications if the object is constant.
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The map is immutable therefore cannot be modified") {}
        }

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore it cannot be modified") {}
        }

        val keyPrimitive = key.getPrimitive()
        val keyHash = keyPrimitive.getHashCode(memory)
        val currentProperty = getOwnPropertyDescriptorInCurrent(keyPrimitive, keyHash)
        val lastProperty = (oldVersion as? LxmMap)?.getOwnPropertyDescriptor(keyPrimitive, keyHash)

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
                properties.putIfAbsent(keyHash, mutableListOf())
                properties[keyHash]!!.add(property)
                property.replaceValue(memory, LxmNil)
            }
        }
    }

    /**
     * Makes the map constant.
     */
    fun makeConstant(memory: LexemMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The map is immutable therefore cannot be modified") {}
        }

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
            getOwnPropertyDescriptorInCurrent(key, hash) ?: (oldVersion as? LxmMap)?.getOwnPropertyDescriptor(key, hash)

    /**
     * Gets the size of the map.
     */
    fun getSize(): Int {
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

            currentObject = currentObject.oldVersion as? LxmMap
        }

        return result.map { it.value.size }.sum()
    }

    /**
     * Gets all properties of the map.
     */
    fun getAllProperties(): MutableMap<Int, MutableList<LxmMapProperty>> {
        val versions = getListOfVersions<LxmMap>()

        // Iterate to get a list of versions.
        val result = mutableMapOf<Int, MutableList<LxmMapProperty>>()

        while (versions.isNotEmpty()) {
            val element = versions.removeLast()
            result.putAll(element.properties)
        }

        return result
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryShift(memory: LexemMemory) = if (isConstant) {
        this
    } else {
        LxmMap(memory, this, toClone = countOldVersions() >= Consts.Memory.maxVersionCountToFullyCopyAValue)
    }

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


        if (!isConstant) {
            this.properties.clear()
        }
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        for ((_, list) in getAllProperties()) {
            for (property in list) {
                property.key.spatialGarbageCollect(memory, gcFifo)
                property.value.spatialGarbageCollect(memory, gcFifo)
            }
        }
    }

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, MapType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: LexemMemory) = LxmString.MapToString

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
            // Keep this to replace the elements before possibly remove the references.
            val oldKey = key
            key = newKey
            memory.replacePrimitives(oldKey, newKey)
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
