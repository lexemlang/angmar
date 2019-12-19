package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*

/**
 * The Lexem value of a List iterator.
 */
internal class LxmSetIterator : LexemIterator {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, set: LxmSet) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Value, set.getPrimitive())

        val list = LxmList(memory)
        val values = set.getAllValues().flatMap { it.value.map { it.value } }
        for (v in values) {
            list.addCell(memory, v)
        }

        setProperty(memory, AnalyzerCommons.Identifiers.Keys, list)

        size = values.size.toLong()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmSetIterator, toClone: Boolean) : super(memory, oldVersion,
            toClone) {
        this.size = oldVersion.size
    }

    // METHODS ----------------------------------------------------------------

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
        val keys = getKeys(memory)
        val currentValue = keys.getCell(memory, index.primitive)!!
        return Pair(null, currentValue)
    }

    override fun memoryShift(memory: LexemMemory) =
            LxmSetIterator(memory, this, toClone = countOldVersions() >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[Iterator - Set] ${super.toString()}"
}
