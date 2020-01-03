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

    constructor(memory: IMemory, map: LxmMap) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Value, map.getPrimitive())

        val list = LxmList(memory)
        list.addCell(memory, *map.getAllProperties().map { it.key }.toList().toTypedArray())

        setProperty(memory, AnalyzerCommons.Identifiers.Keys, list)

        intervalSize = map.size.toLong()
    }

    private constructor(memory: IMemory, oldVersion: LxmMapIterator) : super(memory, oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's map.
     */
    private fun getMap(memory: IMemory) =
            getDereferencedProperty<LxmMap>(memory, AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    /**
     * Gets the iterator's keys.
     */
    private fun getKeys(memory: IMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Keys, toWrite = false)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(memory: IMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val index = getIndex(memory)
        val map = getMap(memory)
        val keys = getKeys(memory)
        val currentKey = keys.getCell(index.primitive)!!
        val currentValue = map.getPropertyValue(currentKey) ?: LxmNil
        return Pair(currentKey, currentValue)
    }

    override fun memoryClone(memory: IMemory) = LxmMapIterator(memory, this)

    override fun toString() = "[Iterator - Map] ${super.toString()}"
}
