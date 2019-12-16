package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*

/**
 * The Lexem value for a quantified group.
 */
internal class LxmQuantifiedGroup : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(mainUnion: LxmReference, memory: LexemMemory) : super(memory) {
        setMainUnion(memory, mainUnion)
        setFinishedCount(memory, LxmInteger.Num0)

        val list = LxmList(memory)
        setProperty(memory, AnalyzerCommons.Identifiers.List, memory.add(list))
    }

    private constructor(memory: LexemMemory, oldContext: LxmQuantifiedGroup, toClone: Boolean) : super(memory,
            oldContext, toClone)

    // METHODS ----------------------------------------------------------------

    fun getFinishedCount(memory: LexemMemory) =
            getDereferencedProperty<LxmInteger>(memory, AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    fun setFinishedCount(memory: LexemMemory, value: LxmInteger) =
            setProperty(memory, AnalyzerCommons.Identifiers.Value, value)

    /**
     * Gets the main union.
     */
    fun getMainUnion(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmPatternUnion>(memory, AnalyzerCommons.Identifiers.Index, toWrite)!!

    /**
     * Sets the main union.
     */
    private fun setMainUnion(memory: LexemMemory, union: LxmReference) =
            setProperty(memory, AnalyzerCommons.Identifiers.Index, union)

    /**
     * Gets the union list.
     */
    private fun getUnionList(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.List, toWrite)!!

    /**
     * Adds a new union to the list.
     */
    fun addUnion(memory: LexemMemory, unionRef: LxmReference) {
        val union = unionRef.dereferenceAs<LxmPatternUnion>(memory, toWrite = false)!!

        if (union.quantifier.min == 0) {
            val index = getFinishedCount(memory)
            setFinishedCount(memory, LxmInteger.from(index.primitive + 1))
        }

        getUnionList(memory, toWrite = true).addCell(memory, unionRef)
    }

    /**
     * Calculates the main quantifier bounds.
     */
    fun calculateMainUnion(memory: LexemMemory) {
        var mainUnion = getMainUnion(memory, toWrite = false)
        if (mainUnion.quantifier.min < 0 || mainUnion.quantifier.max < 0) {
            var min = 0
            var max = 0
            var isInfinite = false

            for (qtf in getUnionList(memory, toWrite = false).getAllCells().map {
                it.dereference(memory, toWrite = false) as LxmPatternUnion
            }) {
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
        val mainUnion = getMainUnion(memory, toWrite = true)
        val list = getUnionList(memory, toWrite = false)
        val cell = list.getCell(memory, optionIndex)!!.dereference(memory, toWrite = true) as LxmPatternUnion
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
        val list = getUnionList(memory, toWrite = false)
        val cell = list.getCell(memory, optionIndex)!!.dereference(memory, toWrite = false) as LxmPatternUnion

        return cell.canHaveANextPattern(memory)
    }

    /**
     * Checks whether the group is finished or not.
     */
    fun isGroupFinished(memory: LexemMemory): Boolean {
        if (!getMainUnion(memory, toWrite = false).isFinished(memory)) {
            return false
        }

        val listSize = getUnionList(memory, toWrite = false).actualListSize
        val finished = getFinishedCount(memory)

        return listSize == finished.primitive
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun clone(memory: LexemMemory) = LxmQuantifiedGroup(memory, this,
            toClone = (countOldVersions() ?: 0) >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[QUANTIFIED GROUP] ${super.toString()}"
}
