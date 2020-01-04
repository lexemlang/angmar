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
    private var isPropertiesCloned = true

    var isConstant = false
        private set
    var size = 0
        private set

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: IMemory) : super(memory)

    private constructor(memory: IMemory, oldVersion: LxmMap) : super(memory, oldVersion) {
        isConstant = oldVersion.isConstant
        properties = oldVersion.properties
        size = oldVersion.size
        isPropertiesCloned = false
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the value of a property.
     */
    fun getPropertyValue(key: LexemMemoryValue): LexemPrimitive? {
        val (list, index) = getPropertyPosition(key.getPrimitive(), key.getPrimitive().getHashCode()) ?: return null
        return list[index].value
    }

    /**
     * Gets the final value of a property.
     */
    inline fun <reified T : LexemMemoryValue> getDereferencedProperty(memory: IMemory, key: LexemPrimitive,
            toWrite: Boolean): T? = getPropertyValue(key)?.dereference(memory, toWrite) as? T

    /**
     * Sets a new value to the property or creates a new property with the specified value.
     */
    fun setProperty(memory: IMemory, key: LexemMemoryValue, value: LexemMemoryValue) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The map is immutable therefore cannot be modified") {}
        }

        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore cannot be modified") {}
        }

        val keyPrimitive = key.getPrimitive()
        val keyHash = keyPrimitive.getHashCode()
        val valuePrimitive = value.getPrimitive()
        val listIndex = getPropertyPosition(keyPrimitive, keyHash)

        cloneProperties()
        valuePrimitive.increaseReferences(memory)

        // Add new property.
        if (listIndex == null) {
            val newProperty = LxmMapProperty(memory, this, keyPrimitive)
            newProperty.replaceValue(memory, valuePrimitive)

            properties.putIfAbsent(keyHash, mutableListOf())
            properties[keyHash]!!.add(newProperty)
            size += 1
        }
        // Modify old property.
        else {
            val (list, index) = listIndex
            var property = list[index]

            if (property.belongsTo != this) {
                property = property.clone(memory, this)
                list[index] = property
            }

            property.replaceValue(memory, valuePrimitive)
        }
    }

    /**
     * Returns whether the map contains a property or not.
     */
    fun containsProperty(key: LexemMemoryValue) =
            getPropertyPosition(key.getPrimitive(), key.getPrimitive().getHashCode()) != null

    /**
     * Removes a property.
     */
    fun removeProperty(memory: IMemory, key: LexemMemoryValue) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The map is immutable therefore cannot be modified") {}
        }

        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore cannot be modified") {}
        }

        val keyPrimitive = key.getPrimitive()
        val keyHash = keyPrimitive.getHashCode()
        val (list, index) = getPropertyPosition(keyPrimitive, keyHash) ?: return

        cloneProperties()

        val property = list[index]
        property.value.decreaseReferences(memory)

        list.removeAt(index)

        if (list.isEmpty()) {
            properties.remove(keyHash)
        }

        size -= 1
    }

    /**
     * Makes the map constant.
     */
    fun makeConstant(memory: IMemory) {
        if (isMemoryImmutable(memory)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                    "The map is immutable therefore cannot be modified") {}
        }

        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The map is constant therefore cannot be modified") {}
        }

        isConstant = true
    }

    /**
     * Gets the property descriptor of the specified property in the current map.
     */
    private fun getPropertyPosition(key: LexemPrimitive, hashCode: Int): Pair<MutableList<LxmMapProperty>, Int>? {
        val list = properties[hashCode] ?: return null

        for ((index, property) in list.withIndex()) {
            if (RelationalFunctions.identityEquals(key, property.key)) {
                return Pair(list, index)
            }
        }

        return null
    }

    /**
     * Gets all properties of the map.
     */
    fun getAllProperties() = properties.asSequence().flatMap { it.value.asSequence() }

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

    override fun memoryClone(memory: IMemory) = if (isConstant) {
        this
    } else {
        LxmMap(memory, this)
    }

    override fun memoryDealloc(memory: IMemory) {
        getAllProperties().forEach { (key, value) ->
            key.decreaseReferences(memory)
            value.decreaseReferences(memory)
        }
    }

    override fun spatialGarbageCollect(memory: IMemory, gcFifo: GarbageCollectorFifo) {
        getAllProperties().forEach { (key, value) ->
            key.spatialGarbageCollect(memory, gcFifo)
            value.spatialGarbageCollect(memory, gcFifo)
        }
    }

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, MapType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: IMemory) = LxmString.MapToString

    override fun toString() = StringBuilder().apply {
        append(MapNode.macroName)

        if (isConstant) {
            append(MapNode.constantToken)
        }

        append(MapNode.startToken)

        val text = getAllProperties().take(4).joinToString("${MapNode.elementSeparator} ") {
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
    class LxmMapProperty(memory: IMemory, val belongsTo: LxmMap, val key: LexemPrimitive) {
        var value: LexemPrimitive = LxmNil

        init {
            key.increaseReferences(memory)
        }

        // For deconstructing
        operator fun component1() = key

        operator fun component2() = value

        // METHODS ------------------------------------------------------------

        /**
         * Clones the object property only if it is not constant.
         */
        fun clone(memory: IMemory, belongsTo: LxmMap): LxmMapProperty {
            val property = LxmMapProperty(memory, belongsTo, key)

            property.value = value

            return property
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(memory: IMemory, newValue: LexemPrimitive) {
            if (belongsTo.bigNodeId != memory.getBigNodeId()) {
                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAnImmutableView,
                        "The map property is immutable therefore cannot be modified") {}
            }

            // Keep this to replace the elements before possibly remove the references.
            val oldValue = value
            value = newValue
            MemoryUtils.replacePrimitives(memory, oldValue, newValue)
        }
    }
}
