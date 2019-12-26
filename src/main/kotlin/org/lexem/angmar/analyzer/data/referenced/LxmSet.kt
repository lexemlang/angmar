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
internal class LxmSet : LexemReferenced {
    private var properties = hashMapOf<Int, MutableList<LxmSetProperty>>()
    private var isPropertiesCloned = false

    var isConstant = false
        private set
    val size get() = properties.size

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory)

    private constructor(memory: LexemMemory, oldVersion: LxmSet) : super(memory, oldVersion) {
        isConstant = oldVersion.isConstant
        properties = oldVersion.properties
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
        var property = getOwnPropertyDescriptor(memory, value)

        if (property == null) {
            cloneProperties()

            property = LxmSetProperty()
            properties.putIfAbsent(valueHash, mutableListOf())
            properties[valueHash]!!.add(property)
            property.replaceValue(valuePrimitive)
        }
    }

    /**
     * Returns whether the set contains a property or not.
     */
    fun containsValue(memory: LexemMemory, value: LexemMemoryValue) = getOwnPropertyDescriptor(memory, value) != null

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
        val property = getOwnPropertyDescriptor(memory, value)

        if (property != null) {
            cloneProperties()

            // Remove property.
            val valueHash = valuePrimitive.getHashCode(memory)
            val list = properties[valueHash]!!
            if (list.size == 1) {
                properties.remove(valueHash)
            } else {
                list.remove(property)
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
    private fun getOwnPropertyDescriptor(memory: LexemMemory, value: LexemMemoryValue): LxmSetProperty? {
        val hashCode = value.getPrimitive().getHashCode(memory)
        val list = properties[hashCode] ?: return null

        for (i in list) {
            if (RelationalFunctions.identityEquals(value, i.value)) {
                return i
            }
        }

        return null
    }

    /**
     * Gets all value of the set.
     */
    fun getAllValues() = properties.asSequence().flatMap { it.value.asSequence() }.map { it.value }

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
        LxmSet(memory, oldVersion = this)
    }

    override fun memoryDealloc(memory: LexemMemory) {
        if (isPropertiesCloned) {
            properties.clear()
        }
    }

    override fun spatialGarbageCollect(memory: LexemMemory, gcFifo: GarbageCollectorFifo) {
        for (property in properties.asSequence().flatMap { it.value.asSequence() }) {
            property.value.spatialGarbageCollect(memory, gcFifo)
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

        val text = properties.asSequence().flatMap { it.value.asSequence() }.take(4).joinToString(", ") {
            it.value.toString()
        }

        append(text)
        append(ListNode.endToken)

        if (size > 4) {
            append(" and ${size - 4} more")
        }
    }.toString()

    // STATIC -----------------------------------------------------------------

    /**
     * A property of [LxmSet]s.
     */
    class LxmSetProperty() {
        var value: LexemPrimitive = LxmNil

        // METHODS ------------------------------------------------------------

        /**
         * Clones the object property only if it is not constant.
         */
        fun clone(): LxmSetProperty {
            val property = LxmSetProperty()

            property.replaceValue(value)

            return property
        }

        /**
         * Replaces the value handling the memory references.
         */
        fun replaceValue(newValue: LexemPrimitive) {
            value = newValue
        }
    }
}
