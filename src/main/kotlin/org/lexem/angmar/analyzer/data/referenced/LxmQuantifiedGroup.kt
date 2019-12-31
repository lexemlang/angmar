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
        setMainUnion(mainUnion)
        setFinishedCount(LxmInteger.Num0)

        val list = LxmList(memory)
        setProperty(AnalyzerCommons.Identifiers.List, list)
    }

    private constructor(bigNode: BigNode, oldVersion: LxmQuantifiedGroup) : super(bigNode, oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the final count.
     */
    fun getFinishedCount() = getDereferencedProperty<LxmInteger>(AnalyzerCommons.Identifiers.Value, toWrite = false)!!

    /**
     * Sets the final count.
     */
    fun setFinishedCount(value: LxmInteger) = setProperty(AnalyzerCommons.Identifiers.Value, value)

    /**
     * Gets the main union.
     */
    fun getMainUnion(toWrite: Boolean) =
            getDereferencedProperty<LxmPatternUnion>(AnalyzerCommons.Identifiers.Index, toWrite)!!

    /**
     * Sets the main union.
     */
    private fun setMainUnion(union: LxmPatternUnion) =
            setProperty(AnalyzerCommons.Identifiers.Index, union.getPrimitive())

    /**
     * Gets the union list.
     */
    private fun getUnionList(toWrite: Boolean) =
            getDereferencedProperty<LxmList>(AnalyzerCommons.Identifiers.List, toWrite)!!

    /**
     * Adds a new union to the list.
     */
    fun addUnion(union: LxmPatternUnion) {
        if (union.quantifier.min == 0) {
            val index = getFinishedCount()
            setFinishedCount(LxmInteger.from(index.primitive + 1))
        }

        getUnionList(toWrite = true).addCell(union.getPrimitive())
    }

    /**
     * Calculates the main quantifier bounds.
     */
    fun calculateMainUnion(memory: LexemMemory) {
        var mainUnion = getMainUnion(toWrite = false)
        if (mainUnion.quantifier.min < 0 || mainUnion.quantifier.max < 0) {
            var min = 0
            var max = 0
            var isInfinite = false

            for (qtf in getUnionList(toWrite = false).getAllCells().map {
                it.dereference(bigNode, toWrite = false) as LxmPatternUnion
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

            setMainUnion(mainUnion)
        }
    }

    /**
     * Increases the index of the specified option.
     */
    fun increaseIndex(optionIndex: Int) {
        val mainUnion = getMainUnion(toWrite = true)
        val list = getUnionList(toWrite = false)
        val cell = list.getCell(optionIndex)!!.dereference(bigNode, toWrite = true) as LxmPatternUnion
        val prevFinished = cell.isFinished()

        mainUnion.increaseIndex()
        cell.increaseIndex()

        if (!prevFinished && cell.isFinished()) {
            val index = getFinishedCount()
            setFinishedCount(LxmInteger.from(index.primitive + 1))
        }
    }

    /**
     * Checks whether the specified group option can have another pattern.
     */
    fun canHaveANextPattern(optionIndex: Int): Boolean {
        val list = getUnionList(toWrite = false)
        val cell = list.getCell(optionIndex)!!.dereference(bigNode, toWrite = false) as LxmPatternUnion

        return cell.canHaveANextPattern()
    }

    /**
     * Checks whether the group is finished or not.
     */
    fun isGroupFinished(): Boolean {
        if (!getMainUnion(toWrite = false).isFinished()) {
            return false
        }

        val listSize = getUnionList(toWrite = false).size
        val finished = getFinishedCount()

        return listSize == finished.primitive
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(bigNode: BigNode) = LxmQuantifiedGroup(bigNode, this)

    override fun toString() = "[Quantified Group] ${super.toString()}"
}
