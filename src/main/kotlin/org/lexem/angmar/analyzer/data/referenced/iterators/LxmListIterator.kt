package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value of a List iterator.
 */
internal class LxmListIterator : LexemIterator {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, list: LxmList) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Value, list.getPrimitive())
        intervalSize = list.size.toLong()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmListIterator) : super(memory, oldVersion) {
        intervalSize = oldVersion.intervalSize
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's list.
     */
    private fun getList(memory: LexemMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override val intervalSize: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val value = getList(memory)
        val index = getIndex(memory)
        val currentValue = value.getCell(memory, index.primitive) ?: LxmNil
        return Pair(null, currentValue)
    }

    override fun memoryShift(memory: LexemMemory) = LxmListIterator(memory, oldVersion = this)

    override fun toString() = "[Iterator - List] ${super.toString()}"
}
