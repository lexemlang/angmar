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

    constructor(memory: IMemory, list: LxmList) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Value, list.getPrimitive())
        intervalSize = list.size.toLong()
    }

    private constructor(memory: IMemory, oldVersion: LxmListIterator) : super(memory, oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's list.
     */
    private fun getList(memory: IMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(memory: IMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val value = getList(memory)
        val index = getIndex(memory)
        val currentValue = value.getCell(index.primitive) ?: LxmNil
        return Pair(null, currentValue)
    }

    override fun memoryClone(memory: IMemory) = LxmListIterator(memory, this)

    override fun toString() = "[Iterator - List] ${super.toString()}"
}
