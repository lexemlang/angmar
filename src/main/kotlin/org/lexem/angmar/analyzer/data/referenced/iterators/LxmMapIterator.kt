package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value of a Map iterator.
 */
internal class LxmMapIterator : LexemIterator {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, map: LxmMap) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Value, map.getPrimitive())

        val list = LxmList(memory)
        for ((key, _) in map.getAllProperties()) {
            list.addCell(memory, key)
        }

        setProperty(memory, AnalyzerCommons.Identifiers.Keys, list)

        intervalSize = map.size.toLong()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmMapIterator) : super(memory, oldVersion) {
        intervalSize = oldVersion.intervalSize
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

    override val intervalSize: Long

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

    override fun memoryShift(memory: LexemMemory) = LxmMapIterator(memory, oldVersion = this)

    override fun toString() = "[Iterator - Map] ${super.toString()}"
}
