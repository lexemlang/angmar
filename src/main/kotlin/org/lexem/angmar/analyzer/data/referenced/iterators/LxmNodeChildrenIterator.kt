package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value of a Node children iterator.
 */
internal class LxmNodeChildrenIterator : LexemIterator {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, parentNode: LxmNode) : super(memory) {
        val firstChild = parentNode.getFirstChild(memory, toWrite = false)

        if (firstChild != null) {
            setProperty(memory, AnalyzerCommons.Identifiers.Value, firstChild)
            setProperty(memory, AnalyzerCommons.Identifiers.Accumulator, firstChild)
        }

        intervalSize = parentNode.getChildCount(memory).toLong()
    }

    private constructor(memory: LexemMemory, oldVersion: LxmNodeChildrenIterator) : super(memory, oldVersion) {
        intervalSize = oldVersion.intervalSize
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the iterator's initial element.
     */
    private fun getInitial(memory: LexemMemory) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    /**
     * Gets the iterator's current element.
     */
    private fun getCurrentElement(memory: LexemMemory) =
            getPropertyValue(memory, AnalyzerCommons.Identifiers.Accumulator)!!

    // OVERRIDE METHODS -------------------------------------------------------

    override val intervalSize: Long

    override fun getCurrent(memory: LexemMemory): Pair<LexemPrimitive?, LexemPrimitive>? {
        if (isEnded(memory)) {
            return null
        }

        return Pair(null, getCurrentElement(memory))
    }

    override fun advance(memory: LexemMemory) {
        super.advance(memory)

        if (isEnded(memory)) {
            removeProperty(memory, AnalyzerCommons.Identifiers.Accumulator)
        } else {
            val current = getCurrentElement(memory).dereference(memory, toWrite = false) as LxmNode
            val next = current.getRightSibling(memory, toWrite = false)!!

            setProperty(memory, AnalyzerCommons.Identifiers.Accumulator, next)
        }
    }

    override fun restart(memory: LexemMemory) {
        super.restart(memory)

        setProperty(memory, AnalyzerCommons.Identifiers.Accumulator, getInitial(memory))
    }

    override fun memoryShift(memory: LexemMemory) = LxmNodeChildrenIterator(memory, oldVersion = this)

    override fun toString() = "[Iterator - List] ${super.toString()}"
}
