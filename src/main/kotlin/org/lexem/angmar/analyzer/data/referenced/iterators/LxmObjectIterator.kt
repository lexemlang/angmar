package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value of an Object iterator.
 */
internal class LxmObjectIterator : LexemIterator {
    private val keys: List<String>

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, obj: LxmObject) : super(memory) {
        setProperty(AnalyzerCommons.Identifiers.Value, obj.getPrimitive())

        keys = obj.getAllIterableProperties().map { it.key }.toList()
        intervalSize = keys.size.toLong()
    }

    private constructor(bigNode: BigNode, oldVersion: LxmObjectIterator) : super(bigNode, oldVersion) {
        this.keys = oldVersion.keys
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's object.
     */
    private fun getObject() = getDereferencedProperty<LxmObject>(AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded()) {
            return null
        }

        val index = getIndex()
        val value = getObject()
        val currentKey = keys[index.primitive]
        val currentValue = value.getPropertyValue(currentKey) ?: LxmNil
        return Pair(LxmString.from(currentKey), currentValue)
    }

    override fun memoryClone(bigNode: BigNode) = LxmObjectIterator(bigNode, this)

    override fun toString() = "[Iterator - Object] ${super.toString()}"
}
