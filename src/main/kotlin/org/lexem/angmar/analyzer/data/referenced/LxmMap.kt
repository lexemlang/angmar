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
internal class LxmMap : LexemReferenced {
    private var properties = hashMapOf<Int, MutableList<LxmMapProperty>>()
    private var isPropertiesCloned = false

    var isConstant = false
        private set
    val size get() = properties.size

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory)

    private constructor(memory: LexemMemory, oldVersion: LxmMap) : super(memory, oldVersion) {
        isConstant = oldVersion.isConstant
        properties = oldVersion.properties
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a property.
     */
    fun getPropertyValue(memory: LexemMemory, key: LexemMemoryValue): LexemPrimitive? {
        val property = getOwnPropertyDescriptor(memory, key) ?: return null
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

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore cannot be modified") {}
        }

        cloneProperties()

        val keyPrimitive = key.getPrimitive()
        val valuePrimitive = value.getPrimitive()
        val keyHash = keyPrimitive.getHashCode(memory)
        var property = getOwnPropertyDescriptor(memory, key)

        if (property == null) {
            property = LxmMapProperty(this)
            properties.putIfAbsent(keyHash, mutableListOf())
            properties[keyHash]!!.add(property)
            property.replaceKey(keyPrimitive)
            property.replaceValue(valuePrimitive)
        } else {
            if (property.belongsTo != this) {
                property = property.clone(this)
            }

            property.replaceValue(valuePrimitive)
        }
    }

    /**
     * Returns whether the map contains a property or not.
     */
    fun containsProperty(memory: LexemMemory, key: LexemMemoryValue) = getOwnPropertyDescriptor(memory, key) != null

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
        val property = getOwnPropertyDescriptor(memory, key)

        if (property != null) {
            cloneProperties()

            // Remove property.
            val keyHash = keyPrimitive.getHashCode(memory)
            val list = properties[keyHash]!!
            if (list.size == 1) {
                properties.remove(keyHash)
            } else {
                list.remove(property)
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
    private fun getOwnPropertyDescriptor(memory: LexemMemory, key: LexemMemoryValue): LxmMapProperty? {
        val hashCode = key.getPrimitive().getHashCode(memory)
        val list = properties[hashCode] ?: return null

        for (i in list) {
            if (RelationalFunctions.identityEquals(key, i.key)) {
                return i
            }
        }

        return null
    }

    /**
     * Gets all properties of the map.
     */
    fun getAllProperties() = properties.asSequence().flatMap { it.value.asSequence() }.map { Pair(it.key, it.value) }

    /**
     * Clones the properties map.
     */
    private fun cloneProperties() {
        if (!isPropertiesCloned) {
            properties = HashMap(properties.mapValues { it.value.toMutableList() })
            isPropertiesCloned = true
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryShift(memory: LexemMemory) = if (isConstant) {
        this
    } else {
        LxmMap(memory, oldVersion = this)
    }

    override fun memoryDealloc(memory: LexemMemory) {
        if (isPropertiesCloned) {
            properties.clear()
        }
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        for (property in properties.asSequence().flatMap { it.value.asSequence() }) {
            property.key.spatialGarbageCollect(memory, gcFifo)
            property.value.spatialGarbageCollect(memory, gcFifo)
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

        val text = properties.asSequence().flatMap { it.value.asSequence() }.take(4)
                .joinToString("${MapNode.elementSeparator} ") {
                    "${it.key}${MapElementNode.keyValueSeparator} ${it.value}"
                }

        append(text)
        append(MapNode.endToken)

        if (size > 4) {
            append(" and ${size - 4} more")
        }
    }.toString()

    // STATIC -----------------------------------------------------------------

    /**
     * A property of [LxmMap]s.
     */
    class LxmMapProperty(val belongsTo: LxmMap) {
        var key: LexemPrimitive = LxmNil
        var value: LexemPrimitive = LxmNil

        // METHODS ------------------------------------------------------------

        /**
         * Clones the object property only if it is not constant.
         */
        fun clone(belongsTo: LxmMap): LxmMapProperty {
            val property = LxmMapProperty(belongsTo)

            property.replaceKey(key)
            property.replaceValue(value)

            return property
        }

        /**
         * Replaces the key handling the memory references.
         */
        fun replaceKey(newKey: LexemPrimitive) {
            key = newKey
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(newValue: LexemPrimitive) {
            value = newValue
        }
    }
}
