package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value of a List iterator.
 */
internal class LxmSetIterator : LexemIterator {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: IMemory, set: LxmSet) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Value, set.getPrimitive())

        val list = LxmList(memory)
        list.addCell(memory, *set.getAllValues().toList().toTypedArray())

        setProperty(memory, AnalyzerCommons.Identifiers.Keys, list)

        intervalSize = set.size.toLong()
    }

    private constructor(memory: IMemory, oldVersion: LxmSetIterator) : super(memory, oldVersion)

    // METHODS ----------------------------------------------------------------

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
        val keys = getKeys(memory)
        val currentValue = keys.getCell(index.primitive)!!
        return Pair(null, currentValue)
    }

    override fun memoryClone(memory: IMemory) = LxmSetIterator(memory, this)

    override fun toString() = "[Iterator - Set] ${super.toString()}"
}
