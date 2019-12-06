package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value for a quantified group.
 */
internal class LxmQuantifiedGroup : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(mainUnion: LxmReference, memory: LexemMemory) {
        setMainUnion(memory, mainUnion)
        setFinishedCount(memory, LxmInteger.Num0)

        val list = LxmList()
        setProperty(memory, AnalyzerCommons.Identifiers.List, memory.add(list))
    }

    constructor(oldContext: LxmQuantifiedGroup) : super(oldContext)

    // METHODS ----------------------------------------------------------------

    fun getFinishedCount(memory: LexemMemory) =
            getDereferencedProperty<LxmInteger>(memory, AnalyzerCommons.Identifiers.Value)!!

    fun setFinishedCount(memory: LexemMemory, value: LxmInteger) =
            setProperty(memory, AnalyzerCommons.Identifiers.Value, value)

    /**
     * Gets the main union.
     */
    fun getMainUnion(memory: LexemMemory) =
            getDereferencedProperty<LxmPatternUnion>(memory, AnalyzerCommons.Identifiers.Index)!!

    /**
     * Sets the main union.
     */
    private fun setMainUnion(memory: LexemMemory, union: LxmReference) =
            setProperty(memory, AnalyzerCommons.Identifiers.Index, union)

    /**
     * Gets the union list.
     */
    private fun getUnionList(memory: LexemMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.List)!!

    /**
     * Adds a new union to the list.
     */
    fun addUnion(memory: LexemMemory, unionRef: LxmReference) {
        val union = unionRef.dereferenceAs<LxmPatternUnion>(memory)!!

        if (union.quantifier.min == 0) {
            val index = getFinishedCount(memory)
            setFinishedCount(memory, LxmInteger.from(index.primitive + 1))
        }

        getUnionList(memory).addCell(memory, unionRef)
    }

    /**
     * Calculates the main quantifier bounds.
     */
    fun calculateMainUnion(memory: LexemMemory) {
        var mainUnion = getMainUnion(memory)
        if (mainUnion.quantifier.min < 0 || mainUnion.quantifier.max < 0) {
            var min = 0
            var max = 0
            var isInfinite = false

            for (qtf in getUnionList(memory).getAllCells().map { it.dereference(memory) as LxmPatternUnion }) {
                min += qtf.quantifier.min

                if (qtf.quantifier.isInfinite) {
                    isInfinite = true
                } else {
                    max += qtf.quantifier.max
                }
            }

            if (mainUnion.quantifier.min >= 0) {
                min = maxOf(min, mainUnion.quantifier.min)
            }

            if (mainUnion.quantifier.max >= 0) {
                max = minOf(max, mainUnion.quantifier.max)
                isInfinite = false
            }

            mainUnion = if (isInfinite) {
                LxmPatternUnion(LxmQuantifier(min, isInfinite = isInfinite), LxmInteger.Num0, memory)
            } else {
                LxmPatternUnion(LxmQuantifier(min, max), LxmInteger.Num0, memory)
            }

            setMainUnion(memory, memory.add(mainUnion))
        }
    }

    /**
     * Increases the index of the specified option.
     */
    fun increaseIndex(memory: LexemMemory, optionIndex: Int) {
        val mainUnion = getMainUnion(memory)
        val list = getUnionList(memory)
        val cell = list.getCell(memory, optionIndex)!!.dereference(memory) as LxmPatternUnion
        val prevFinished = cell.isFinished(memory)

        mainUnion.increaseIndex(memory)
        cell.increaseIndex(memory)

        if (!prevFinished && cell.isFinished(memory)) {
            val index = getFinishedCount(memory)
            setFinishedCount(memory, LxmInteger.from(index.primitive + 1))
        }
    }

    /**
     * Checks whether the specified group option can have another pattern.
     */
    fun canHaveANextPattern(memory: LexemMemory, optionIndex: Int): Boolean {
        val list = getUnionList(memory)
        val cell = list.getCell(memory, optionIndex)!!.dereference(memory) as LxmPatternUnion

        return cell.canHaveANextPattern(memory)
    }

    /**
     * Checks whether the group is finished or not.
     */
    fun isGroupFinished(memory: LexemMemory): Boolean {
        if (!getMainUnion(memory).isFinished(memory)) {
            return false
        }

        val listSize = getUnionList(memory).actualListSize
        val finished = getFinishedCount(memory)

        return listSize == finished.primitive
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun clone() = LxmQuantifiedGroup(this)

    override fun toString() = "[QUANTIFIED GROUP] ${super.toString()}"
}
