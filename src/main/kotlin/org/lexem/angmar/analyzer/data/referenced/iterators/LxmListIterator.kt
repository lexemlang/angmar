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

    constructor(memory: LexemMemory, value: LxmReference) : super(memory) {
        val list = value.dereferenceAs<LxmList>(memory, toWrite = false)!!
        setProperty(memory, AnalyzerCommons.Identifiers.Value, value)
        this.size = list.actualListSize.toLong()
    }

    private constructor(memory: LexemMemory, oldIterator: LxmListIterator) : super(memory, oldIterator) {
        this.size = oldIterator.size
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's list.
     */
    private fun getList(memory: LexemMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override val size: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val value = getList(memory)
        val index = getIndex(memory)
        val currentValue = value.getCell(memory, index.primitive) ?: LxmNil
        return Pair(null, currentValue)
    }

    override fun clone(memory: LexemMemory) = LxmListIterator(memory, this)

    override fun toString() = "[ITERATOR - LIST] ${super.toString()}"
}
