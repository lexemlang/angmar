package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * The Lexem values of a Quantifier value.
 */
internal class LxmQuantifier : LexemPrimitive {
    val min: Int
    val max: Int
    val isInfinite: Boolean
    var isLazy: Boolean
    var isAtomic: Boolean

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(min: Int, isInfinite: Boolean = false, isLazy: Boolean = false, isAtomic: Boolean = false) {
        this.min = min
        this.max = min
        this.isInfinite = isInfinite
        this.isLazy = isLazy
        this.isAtomic = isAtomic
    }

    constructor(min: Int, max: Int, isLazy: Boolean = false, isAtomic: Boolean = false) {
        this.min = min
        this.max = max
        this.isInfinite = false
        this.isLazy = isLazy
        this.isAtomic = isAtomic
    }

    constructor(quantifier: LxmQuantifier, isLazy: Boolean = false, isAtomic: Boolean = false) {
        this.min = quantifier.min
        this.max = quantifier.max
        this.isInfinite = quantifier.isInfinite
        this.isLazy = isLazy
        this.isAtomic = isAtomic
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Checks whether the quantifier can have a next iteration regarding its bounds.
     */
    fun canHaveANextIteration(currentIndex: Int) = if (isInfinite) {
        true
    } else {
        currentIndex < max
    }

    /**
     * Checks whether the quantifier is finished or not.
     */
    fun isFinished(currentIndex: Int) = if (isInfinite) {
        currentIndex >= min
    } else {
        currentIndex in min..max
    }

    // OVERRIDE METHODS -------------------------------------------------------


    override fun getHashCode(memory: LexemMemory): Int {
        var result = min
        result = 31 * result + max
        result = 31 * result + isInfinite.hashCode()
        return result
    }

    override fun toString(): String {
        var result = when {
            isInfinite -> "{$min,}"
            min != max -> "{$min, $max}"
            else -> "{$min}"
        }

        if (isAtomic) {
            result += if (isLazy) {
                QuantifierLexemeNode.atomicLazyAbbreviations
            } else {
                QuantifierLexemeNode.atomicGreedyAbbreviations
            }
        } else if (isLazy) {
            result += QuantifierLexemeNode.lazyAbbreviation
        }

        return result
    }

    // STATIC -----------------------------------------------------------------

    companion object {
        val GreedyZeroOrMore = LxmQuantifier(0, isInfinite = true)
        val GreedyOneOrMore = LxmQuantifier(1, isInfinite = true)
        val GreedyZeroOrOne = LxmQuantifier(0, 1)
        val LazyZeroOrMore = LxmQuantifier(0, isInfinite = true, isLazy = true)
        val LazyOneOrMore = LxmQuantifier(1, isInfinite = true, isLazy = true)
        val LazyZeroOrOne = LxmQuantifier(0, 1, isLazy = true)
        val AtomicGreedyZeroOrMore = LxmQuantifier(0, isInfinite = true, isAtomic = true)
        val AtomicGreedyOneOrMore = LxmQuantifier(1, isInfinite = true, isAtomic = true)
        val AtomicGreedyZeroOrOne = LxmQuantifier(0, 1, isAtomic = true)
        val AtomicLazyZeroOrMore = LxmQuantifier(0, isInfinite = true, isLazy = true, isAtomic = true)
        val AtomicLazyOneOrMore = LxmQuantifier(1, isInfinite = true, isLazy = true, isAtomic = true)
        val AtomicLazyZeroOrOne = LxmQuantifier(0, 1, isLazy = true, isAtomic = true)

        // Patterns
        val AdditivePattern = GreedyOneOrMore
        val SelectivePattern = GreedyZeroOrOne
        val AlternativePattern = LxmQuantifier(1, 1)
    }
}
