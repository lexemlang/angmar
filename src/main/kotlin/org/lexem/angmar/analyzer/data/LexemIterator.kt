package org.lexem.angmar.analyzer.data

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * An iterator for an element.
 */
internal abstract class LexemIterator : LxmObject {
    /**
     * The number of iterations.
     */
    var intervalSize = 0L
        protected set

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: IMemory) : super(memory) {
        restart(memory)
    }

    protected constructor(memory: IMemory, oldVersion: LexemIterator) : super(memory, oldVersion) {
        intervalSize = oldVersion.intervalSize
    }

    // METHODS ----------------------------------------------------------------

    /**
     * The current position of the iterator.
     */
    fun getIndex(memory: IMemory) = getPropertyValue(memory, AnalyzerCommons.Identifiers.Index) as LxmInteger

    /**
     * Whether the iterator is ended or not.
     */
    fun isEnded(memory: IMemory) = getIndex(memory).primitive >= intervalSize

    /**
     * Returns the current element in a index-value pair.
     */
    abstract fun getCurrent(memory: IMemory): Pair<LexemPrimitive?, LexemPrimitive>?

    /**
     * Advance one iteration.
     */
    open fun advance(memory: IMemory) {
        val prev = getPropertyValue(memory, AnalyzerCommons.Identifiers.Index) as LxmInteger
        val new = LxmInteger.from(prev.primitive + 1)
        setProperty(memory, AnalyzerCommons.Identifiers.Index, new)
    }

    /**
     * Restarts the iterator.
     */
    open fun restart(memory: IMemory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Index, LxmInteger.Num0)
    }
}
