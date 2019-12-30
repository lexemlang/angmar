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
        setProperty(AnalyzerCommons.Identifiers.Value, list.getPrimitive())
        intervalSize = list.size.toLong()
    }

    private constructor(bigNode: BigNode, oldVersion: LxmListIterator) : super(bigNode, oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's list.
     */
    private fun getList() = getDereferencedProperty<LxmList>(AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded()) {
            return null
        }

        val value = getList()
        val index = getIndex()
        val currentValue = value.getCell(index.primitive) ?: LxmNil
        return Pair(null, currentValue)
    }

    override fun memoryClone(bigNode: BigNode) = LxmListIterator(bigNode, this)

    override fun toString() = "[Iterator - List] ${super.toString()}"
}
