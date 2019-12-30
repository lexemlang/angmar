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
        setProperty(AnalyzerCommons.Identifiers.Value, set.getPrimitive())

        val list = LxmList(memory)
        list.addCell(*set.getAllValues().toList().toTypedArray())

        setProperty(AnalyzerCommons.Identifiers.Keys, list)

        intervalSize = set.size.toLong()
    }

    private constructor(bigNode: BigNode, oldVersion: LxmSetIterator) : super(bigNode, oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's keys.
     */
    private fun getKeys() = getDereferencedProperty<LxmList>(AnalyzerCommons.Identifiers.Keys, toWrite = false)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded()) {
            return null
        }

        val index = getIndex()
        val keys = getKeys()
        val currentValue = keys.getCell(index.primitive)!!
        return Pair(null, currentValue)
    }

    override fun memoryClone(bigNode: BigNode) = LxmSetIterator(bigNode, this)

    override fun toString() = "[Iterator - Set] ${super.toString()}"
}
