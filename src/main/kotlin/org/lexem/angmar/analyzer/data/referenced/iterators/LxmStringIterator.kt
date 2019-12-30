package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value of a String iterator.
 */
internal class LxmStringIterator : LexemIterator {
    val value: String

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, value: String) : super(memory) {
        this.value = value
        intervalSize = value.length.toLong()
    }

    private constructor(bigNode: BigNode, oldVersion: LxmStringIterator) : super(bigNode, oldVersion) {
        this.value = oldVersion.value
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getCurrent(): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded()) {
            return null
        }

        val index = getIndex()
        val currentValue = value[index.primitive]
        return Pair(null, LxmString.from("$currentValue"))
    }

    override fun memoryClone(bigNode: BigNode) = LxmStringIterator(bigNode, this)

    override fun toString() = "[Iterator - String] (value: $value) - ${super.toString()}"
}
