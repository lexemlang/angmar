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

    constructor(memory: LexemMemory) : super(memory) {
        restart()
    }

    protected constructor(bigNode: BigNode, oldVersion: LexemIterator) : super(bigNode, oldVersion) {
        intervalSize = oldVersion.intervalSize
    }

    // METHODS ----------------------------------------------------------------

    /**
     * The current position of the iterator.
     */
    fun getIndex() = getPropertyValue(AnalyzerCommons.Identifiers.Index) as LxmInteger

    /**
     * Whether the iterator is ended or not.
     */
    fun isEnded() = getIndex().primitive >= intervalSize

    /**
     * Returns the current element in a index-value pair.
     */
    abstract fun getCurrent(): Pair<LexemPrimitive?, LexemPrimitive>?

    /**
     * Advance one iteration.
     */
    open fun advance() {
        val prev = getPropertyValue(AnalyzerCommons.Identifiers.Index) as LxmInteger
        val new = LxmInteger.from(prev.primitive + 1)
        setProperty(AnalyzerCommons.Identifiers.Index, new)
    }

    /**
     * Restarts the iterator.
     */
    open fun restart() {
        setProperty(AnalyzerCommons.Identifiers.Index, LxmInteger.Num0)
    }
}
