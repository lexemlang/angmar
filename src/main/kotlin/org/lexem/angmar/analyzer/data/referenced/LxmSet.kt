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
    private var properties = hashMapOf<Int, MutableList<LexemPrimitive>>()
    private var isPropertiesCloned = true

    var isConstant = false
        private set
    var size = 0
        private set

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: IMemory) : super(memory)

    private constructor(memory: IMemory, oldVersion: LxmSet) : super(memory, oldVersion) {
        isConstant = oldVersion.isConstant
        properties = oldVersion.properties
        size = oldVersion.size
        isPropertiesCloned = false
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new value to the set.
     */
    fun addValue(memory: IMemory, value: LexemMemoryValue) {
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore cannot be modified") {}
        }

        val valuePrimitive = value.getPrimitive()
        val valueHash = valuePrimitive.getHashCode()
        val property = getPropertyPosition(valuePrimitive, valueHash)
        if (property != null) {
            return
        }

        valuePrimitive.increaseReferences(memory)

        cloneProperties()
        properties.putIfAbsent(valueHash, mutableListOf())
        properties[valueHash]!!.add(valuePrimitive)
        size += 1
    }

    /**
     * Returns whether the set contains a property or not.
     */
    fun containsValue(value: LexemMemoryValue) =
            getPropertyPosition(value.getPrimitive(), value.getPrimitive().getHashCode()) != null

    /**
     * Removes a value.
     */
    fun removeValue(memory: IMemory, value: LexemMemoryValue) {
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore it cannot be modified") {}
        }

        val valuePrimitive = value.getPrimitive()
        val valueHash = valuePrimitive.getHashCode()
        val (list, index) = getPropertyPosition(valuePrimitive, valueHash) ?: return

        cloneProperties()

        list.removeAt(index)
        size -= 1

        if (list.isEmpty()) {
            properties.remove(valueHash)
        }

        value.getPrimitive().decreaseReferences(memory)
    }

    /**
     * Makes the set constant.
     */
    fun makeConstant() {
        if (isConstant) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.CannotModifyAConstantSet,
                    "The set is constant therefore cannot be modified") {}
        }

        isConstant = true
    }

    /**
     * Gets all value of the set.
     */
    fun getAllValues() = properties.asSequence().flatMap { it.value.asSequence() }

    /**
     * Gets the property descriptor of the specified property in the current set.
     */
    private fun getPropertyPosition(value: LexemPrimitive, hashCode: Int): Pair<MutableList<LexemPrimitive>, Int>? {
        val list = properties[hashCode] ?: return null

        for ((index, listValue) in list.withIndex()) {
            if (RelationalFunctions.identityEquals(value, listValue)) {
                return Pair(list, index)
            }
        }

        return null
    }

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

    override fun memoryClone(memory: IMemory) = LxmSet(memory, this)

    override fun memoryDealloc(memory: IMemory) {
        getAllValues().forEach { it.decreaseReferences(memory) }
    }

    override fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) {
        getAllValues().forEach { it.spatialGarbageCollect(gcFifo) }
    }

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, SetType.TypeName) as LxmReference
    }

    override fun toLexemString(memory: IMemory) = LxmString.SetToString

    override fun toString() = StringBuilder().apply {
        append(SetNode.macroName)

        if (isConstant) {
            append(ListNode.constantToken)
        }

        append(ListNode.startToken)

        val text = getAllValues().take(4).joinToString(", ") {
            it.toString()
        }

        append(text)
        append(ListNode.endToken)

        if (size > 4) {
            append(" and ${size - 4} more")
        }
    }.toString()
}
