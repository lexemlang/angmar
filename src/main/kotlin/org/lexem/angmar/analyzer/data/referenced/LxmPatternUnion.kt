package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value for a pattern union.
 */
internal class LxmPatternUnion : LxmObject {
    val quantifier: LxmQuantifier

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(quantifier: LxmQuantifier, index: LxmInteger, memory: LexemMemory) {
        this.quantifier = quantifier
        setIndex(memory, index)
    }

    constructor(oldContext: LxmPatternUnion) : super(oldContext) {
        this.quantifier = oldContext.quantifier
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the index.
     */
    fun getIndex(memory: LexemMemory) = getDereferencedProperty<LxmInteger>(memory, AnalyzerCommons.Identifiers.Index)!!

    /**
     * Sets the index.
     */
    fun setIndex(memory: LexemMemory, index: LxmInteger) {
        setProperty(memory, AnalyzerCommons.Identifiers.Index, index)
    }

    /**
     * Increases the index.
     */
    fun increaseIndex(memory: LexemMemory) {
        val index = getIndex(memory)
        val newIndex = LxmInteger.from(index.primitive + 1)
        setIndex(memory, newIndex)
    }

    /**
     * Checks whether the union can have another pattern regarding its bounds.
     */
    fun canHaveANextPattern(memory: LexemMemory) = quantifier.canHaveANextIteration(getIndex(memory).primitive)

    /**
     * Checks whether the union is finished or not.
     */
    fun isFinished(memory: LexemMemory) = quantifier.isFinished(getIndex(memory).primitive)

    /**
     * Checks whether the union can finish with current state and the remaining patterns.
     */
    fun canFinish(memory: LexemMemory, remaining: Int): Boolean {
        val possible = getIndex(memory).primitive + remaining

        return possible >= quantifier.min
    }

    /**
     * Checks whether the specified quantifier is equals to the union's one.
     */
    fun quantifierIsEqualsTo(other: LxmQuantifier): Boolean {
        if (quantifier.min != other.min) return false
        if (quantifier.max != other.max) return false
        if (quantifier.isInfinite != other.isInfinite) return false
        if (quantifier.isLazy != other.isLazy) return false
        if (quantifier.isAtomic != other.isAtomic) return false

        return true
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun clone() = LxmPatternUnion(this)

    override fun toString() = "[PATTERN UNION] Qtf$quantifier - ${super.toString()}"
}
