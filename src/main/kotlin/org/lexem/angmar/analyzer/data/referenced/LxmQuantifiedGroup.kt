package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value for a quantified group.
 */
internal class LxmQuantifiedGroup : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, mainUnion: LxmPatternUnion) : super(memory) {
        setMainUnion(memory, mainUnion)
        setFinishedCount(memory, LxmInteger.Num0)

        val list = LxmList(memory)
        setProperty(memory, AnalyzerCommons.Identifiers.List, list)
    }

    private constructor(bigNode: BigNode, oldVersion: LxmQuantifiedGroup) : super(bigNode, oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the final count.
     */
    fun getFinishedCount(memory: LexemMemory) =
            getDereferencedProperty<LxmInteger>(memory, AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    /**
     * Sets the final count.
     */
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
    private fun setMainUnion(memory: LexemMemory, union: LxmPatternUnion) =
            setProperty(memory, AnalyzerCommons.Identifiers.Index, union.getPrimitive())

    /**
     * Gets the union list.
     */
    private fun getUnionList(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.List, toWrite)!!

    /**
     * Adds a new union to the list.
     */
    fun addUnion(memory: LexemMemory, union: LxmPatternUnion) {
        if (union.quantifier.min == 0) {
            val index = getFinishedCount(memory)
            setFinishedCount(memory, LxmInteger.from(index.primitive + 1))
        }

        getUnionList(memory, toWrite = true).addCell(memory, union.getPrimitive())
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
                LxmPatternUnion(memory, LxmQuantifier(min, isInfinite = isInfinite), LxmInteger.Num0)
            } else {
                LxmPatternUnion(memory, LxmQuantifier(min, max), LxmInteger.Num0)
            }

            setMainUnion(memory, mainUnion)
        }
    }

    /**
     * Increases the index of the specified option.
     */
    fun increaseIndex(memory: LexemMemory, optionIndex: Int) {
        val mainUnion = getMainUnion(memory, toWrite = true)
        val list = getUnionList(memory, toWrite = false)
        val cell = list.getCell(optionIndex)!!.dereference(memory, toWrite = true) as LxmPatternUnion
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
        val cell = list.getCell(optionIndex)!!.dereference(memory, toWrite = false) as LxmPatternUnion

        return cell.canHaveANextPattern(memory)
    }

    /**
     * Checks whether the group is finished or not.
     */
    fun isGroupFinished(memory: LexemMemory): Boolean {
        if (!getMainUnion(memory, toWrite = false).isFinished(memory)) {
            return false
        }

        val listSize = getUnionList(memory, toWrite = false).size
        val finished = getFinishedCount(memory)

        return listSize == finished.primitive
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(bigNode: BigNode) = LxmQuantifiedGroup(bigNode, this)

    override fun toString() = "[Quantified Group] ${super.toString()}"
}
