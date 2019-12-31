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

    constructor(memory: LexemMemory, quantifier: LxmQuantifier, index: LxmInteger) : super(memory) {
        this.quantifier = quantifier
        setIndex(index)
    }

    private constructor(bigNode: BigNode, oldVersion: LxmPatternUnion) : super(bigNode, oldVersion) {
        this.quantifier = oldVersion.quantifier
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the index.
     */
    fun getIndex() = getDereferencedProperty<LxmInteger>(AnalyzerCommons.Identifiers.Index, toWrite = false)!!

    /**
     * Sets the index.
     */
    fun setIndex(index: LxmInteger) = setProperty(AnalyzerCommons.Identifiers.Index, index)

    /**
     * Increases the index.
     */
    fun increaseIndex() {
        val index = getIndex()
        val newIndex = LxmInteger.from(index.primitive + 1)
        setIndex(newIndex)
    }

    /**
     * Checks whether the union can have another pattern regarding its bounds.
     */
    fun canHaveANextPattern() = quantifier.canHaveANextIteration(getIndex().primitive)

    /**
     * Checks whether the union is finished or not.
     */
    fun isFinished() = quantifier.isFinished(getIndex().primitive)

    /**
     * Checks whether the union can finish with current state and the remaining patterns.
     */
    fun canFinish(remaining: Int): Boolean {
        val possible = getIndex().primitive + remaining

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

    override fun memoryClone(bigNode: BigNode) = LxmPatternUnion(bigNode, this)

    override fun toString() = "[Pattern Union] (Qtf: $quantifier) - ${super.toString()}"
}
