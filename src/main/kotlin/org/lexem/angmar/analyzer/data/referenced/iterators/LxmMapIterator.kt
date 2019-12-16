package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*

/**
 * The Lexem value of a Map iterator.
 */
internal class LxmMapIterator : LexemIterator {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, value: LxmReference) : super(memory) {
        val map = value.dereferenceAs<LxmMap>(memory, toWrite = false)!!
        setProperty(memory, AnalyzerCommons.Identifiers.Value, value)

        val list = LxmList(memory)
        val values = map.getAllProperties().flatMap { it.value.map { it.key } }
        for (v in values) {
            list.addCell(memory, v)
        }

        setProperty(memory, AnalyzerCommons.Identifiers.Keys, memory.add(list))

        size = values.size.toLong()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmMapIterator, toClone: Boolean) : super(memory, oldVersion,
            toClone) {
        this.size = oldVersion.size
    }
    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's map.
     */
    private fun getMap(memory: LexemMemory) =
            getDereferencedProperty<LxmMap>(memory, AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    /**
     * Gets the iterator's keys.
     */
    private fun getKeys(memory: LexemMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Keys, toWrite = false)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override val size: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val index = getIndex(memory)
        val map = getMap(memory)
        val keys = getKeys(memory)
        val currentKey = keys.getCell(memory, index.primitive)!!
        val currentValue = map.getPropertyValue(memory, currentKey) ?: LxmNil
        return Pair(currentKey, currentValue)
    }

    override fun clone(memory: LexemMemory) = LxmMapIterator(memory, this,
            toClone = (countOldVersions() ?: 0) >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[ITERATOR - MAP] ${super.toString()}"
}
