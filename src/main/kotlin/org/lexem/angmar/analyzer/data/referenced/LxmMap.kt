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

    constructor(memory: LexemMemory) : super(memory)

    private constructor(bigNode: BigNode, oldVersion: LxmMap) : super(bigNode, oldVersion) {
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
    inline fun <reified T : LexemMemoryValue> getDereferencedProperty(key: LexemPrimitive, toWrite: Boolean): T? =
            getPropertyValue(key)?.dereference(bigNode, toWrite) as? T

    /**
     * Sets a new value to the property or creates a new property with the specified value.
     */
    fun setProperty(key: LexemMemoryValue, value: LexemMemoryValue) {
        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore cannot be modified") {}
        }

        val keyPrimitive = key.getPrimitive()
        val keyHash = keyPrimitive.getHashCode()
        val valuePrimitive = value.getPrimitive()
        val listIndex = getPropertyPosition(valuePrimitive, keyHash)

        cloneProperties()
        valuePrimitive.increaseReferences(bigNode)

        // Add new property.
        if (listIndex == null) {
            val newProperty = LxmMapProperty(this, keyPrimitive)
            newProperty.replaceValue(valuePrimitive)

            properties.putIfAbsent(keyHash, mutableListOf())
            properties[keyHash]!!.add(newProperty)
            size += 1
        }
        // Modify old property.
        else {
            val (list, index) = listIndex
            var property = list[index]

            if (property.belongsTo != this) {
                property = property.clone(this)
                list[index] = property
            }

            property.replaceValue(valuePrimitive)
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
    fun removeProperty(key: LexemMemoryValue) {
        if (this.isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantMap,
                    "The map is constant therefore cannot be modified") {}
        }

        val keyPrimitive = key.getPrimitive()
        val keyHash = keyPrimitive.getHashCode()
        val (list, index) = getPropertyPosition(keyPrimitive, keyHash) ?: return

        cloneProperties()

        val property = list[index]
        property.value.decreaseReferences(bigNode)

        list.removeAt(index)

        if (list.isEmpty()) {
            properties.remove(keyHash)
        }
    }

    /**
     * Makes the map constant.
     */
    fun makeConstant() {
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The map is constant therefore cannot be modified") {}
        }

        isConstant = true
    }

    /**
     * Gets the property descriptor of the specified property in the current map.
     */
    private fun getPropertyPosition(value: LexemPrimitive, hashCode: Int): Pair<MutableList<LxmMapProperty>, Int>? {
        val list = properties[hashCode] ?: return null

        for ((index, property) in list.withIndex()) {
            if (RelationalFunctions.identityEquals(value, property.key)) {
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

    override fun memoryClone(bigNode: BigNode) = LxmMap(bigNode, this)

    override fun memoryDealloc() {
        getAllProperties().forEach { (key, value) ->
            key.decreaseReferences(bigNode)
            value.decreaseReferences(bigNode)
        }
    }

    override fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) {
        getAllProperties().forEach { (key, value) ->
            key.spatialGarbageCollect(gcFifo)
            value.spatialGarbageCollect(gcFifo)
        }
    }

    override fun getType(bigNode: BigNode): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(bigNode, toWrite = false)
        return context.getPropertyValue(MapType.TypeName) as LxmReference
    }

    override fun toLexemString(bigNode: BigNode) = LxmString.MapToString

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
    class LxmMapProperty(val belongsTo: LxmMap, val key: LexemPrimitive) {
        var value: LexemPrimitive = LxmNil

        init {
            key.increaseReferences(belongsTo.bigNode)
        }

        // For deconstructing
        operator fun component1() = key

        operator fun component2() = value

        // METHODS ------------------------------------------------------------

        /**
         * Clones the object property only if it is not constant.
         */
        fun clone(belongsTo: LxmMap): LxmMapProperty {
            val property = LxmMapProperty(belongsTo, key)

            property.value = value

            return property
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(newValue: LexemPrimitive) {
            // Keep this to replace the elements before possibly remove the references.
            val oldValue = value
            value = newValue
            MemoryUtils.replacePrimitives(belongsTo.bigNode, oldValue, newValue)
        }
    }
}
