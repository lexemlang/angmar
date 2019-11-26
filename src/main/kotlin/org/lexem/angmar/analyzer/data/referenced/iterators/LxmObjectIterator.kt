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

    constructor(memory: LexemMemory, value: LxmReference) : super(memory) {
        val obj = value.dereferenceAs<LxmObject>(memory)!!
        setProperty(memory, AnalyzerCommons.Identifiers.Value, value)

        keys = obj.getAllIterableProperties().map { it.key }
        size = keys.size.toLong()
    }

    private constructor(oldIterator: LxmObjectIterator) : super(oldIterator) {
        this.keys = oldIterator.keys
        this.size = oldIterator.size
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's object.
     */
    private fun getObject(memory: LexemMemory) =
            getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.Value)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override val size: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        val index = getIndex(memory)
        val value = getObject(memory)
        val currentKey = keys[index.primitive]
        val currentValue = value.getPropertyValue(memory, currentKey) ?: LxmNil
        return Pair(LxmString.from(currentKey), currentValue)
    }

    override fun clone() = LxmObjectIterator(this)

    override fun toString() = "[ITERATOR - OBJECT] ${super.toString()}"
}
