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

    constructor(memory: LexemMemory, value: LxmReference) : super(memory) {
        val set = value.dereferenceAs<LxmMap>(memory)!!
        setProperty(memory, AnalyzerCommons.Identifiers.Value, value)

        val list = LxmList()
        val values = set.getAllProperties().flatMap { it.value.map { it.key } }
        for (v in values) {
            list.addCell(memory, v)
        }

        setProperty(memory, AnalyzerCommons.Identifiers.Keys, memory.add(list))

        size = values.size.toLong()
    }

    private constructor(oldIterator: LxmMapIterator) : super(oldIterator) {
        this.size = oldIterator.size
    }
    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's map.
     */
    private fun getMap(memory: LexemMemory) =
            getDereferencedProperty<LxmMap>(memory, AnalyzerCommons.Identifiers.Value)!!

    /**
     * Gets the iterator's keys.
     */
    private fun getKeys(memory: LexemMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Keys)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override val size: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val index = getIndex(memory)
        val map = getMap(memory)
        val keys = getKeys(memory)
        val currentKey = keys.getCell(memory, index.primitive)!!
        val currentValue = map.getPropertyValue(memory, currentKey) ?: LxmNil
        return Pair(currentKey, currentValue)
    }

    override fun clone() = LxmMapIterator(this)

    override fun toString() = "[ITERATOR - MAP] ${super.toString()}"
}
