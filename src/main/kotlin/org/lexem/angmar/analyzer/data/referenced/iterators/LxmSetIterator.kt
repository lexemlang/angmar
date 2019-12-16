package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*

/**
 * The Lexem value of a List iterator.
 */
internal class LxmSetIterator : LexemIterator {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, value: LxmReference) : super(memory) {
        val set = value.dereferenceAs<LxmSet>(memory, toWrite = false)!!
        setProperty(memory, AnalyzerCommons.Identifiers.Value, value)

        val list = LxmList(memory)
        val values = set.getAllValues().flatMap { it.value.map { it.value } }
        for (v in values) {
            list.addCell(memory, v)
        }

        setProperty(memory, AnalyzerCommons.Identifiers.Keys, memory.add(list))

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

    override fun clone(memory: LexemMemory) = LxmSetIterator(memory, this,
            toClone = (countOldVersions() ?: 0) >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[ITERATOR - SET] ${super.toString()}"
}
