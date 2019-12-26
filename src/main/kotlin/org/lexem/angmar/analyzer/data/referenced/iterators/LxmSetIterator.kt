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

    constructor(memory: LexemMemory, set: LxmSet) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Value, set.getPrimitive())

        val list = LxmList(memory)
        for (v in set.getAllValues()) {
            list.addCell(memory, v)
        }

        setProperty(memory, AnalyzerCommons.Identifiers.Keys, list)

        intervalSize = set.size.toLong()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmSetIterator) : super(memory, oldVersion) {
        intervalSize = oldVersion.intervalSize
    }

    // METHODS ----------------------------------------------------------------

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
        val keys = getKeys(memory)
        val currentValue = keys.getCell(memory, index.primitive)!!
        return Pair(null, currentValue)
    }

    override fun memoryShift(memory: LexemMemory) = LxmSetIterator(memory, oldVersion = this)

    override fun toString() = "[Iterator - Set] ${super.toString()}"
}
