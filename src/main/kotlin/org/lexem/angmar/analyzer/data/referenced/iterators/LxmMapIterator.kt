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

    constructor(memory: LexemMemory, map: LxmMap) : super(memory) {
        setProperty(AnalyzerCommons.Identifiers.Value, map.getPrimitive())

        val list = LxmList(memory)
        list.addCell(*map.getAllProperties().map { it.key }.toList().toTypedArray())

        setProperty(AnalyzerCommons.Identifiers.Keys, list)

        intervalSize = map.size.toLong()
    }

    private constructor(bigNode: BigNode, oldVersion: LxmMapIterator) : super(bigNode, oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's map.
     */
    private fun getMap() = getDereferencedProperty<LxmMap>(AnalyzerCommons.Identifiers.Value, toWrite = false)!!

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
        val map = getMap()
        val keys = getKeys()
        val currentKey = keys.getCell(index.primitive)!!
        val currentValue = map.getPropertyValue(currentKey) ?: LxmNil
        return Pair(currentKey, currentValue)
    }

    override fun memoryClone(bigNode: BigNode) = LxmMapIterator(bigNode, this)

    override fun toString() = "[Iterator - Map] ${super.toString()}"
}
