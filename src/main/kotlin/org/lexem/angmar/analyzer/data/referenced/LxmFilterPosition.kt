package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value for a pattern union.
 */
internal class LxmFilterPosition : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: IMemory, parent: LxmNode) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Parent, parent)

        if (parent.getChildCount(memory) > 0) {
            setProperty(memory, AnalyzerCommons.Identifiers.Value, parent.getFirstChild(memory, toWrite = false)!!)
        }
    }

    private constructor(memory: IMemory, oldVersion: LxmFilterPosition) : super(memory, oldVersion = oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the parent node.
     */
    fun getParent(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.Parent, toWrite)!!

    /**
     * Gets the current node.
     */
    fun getCurrent(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.Value, toWrite)

    /**
     * Gets the previous node.
     */
    fun getPrevious(memory: IMemory, toWrite: Boolean) =
            getCurrent(memory, toWrite = false)?.getRightSibling(memory, toWrite) ?: getParent(memory,
                    toWrite = false).getLastChild(memory, toWrite)

    /**
     * Advances the index one position.
     */
    fun advance(memory: IMemory) {
        val current = getCurrent(memory, toWrite = false) ?: return
        val next = current.getRightSibling(memory, toWrite = false)

        if (next == null) {
            removeProperty(memory, AnalyzerCommons.Identifiers.Value)
        } else {
            setProperty(memory, AnalyzerCommons.Identifiers.Value, next)
        }
    }

    /**
     * Advances backwards the index one position.
     */
    fun advanceBack(memory: IMemory) {
        val current = getCurrent(memory, toWrite = false)

        if (current == null) {
            val lastChild = getParent(memory, toWrite = false).getLastChild(memory, toWrite = false)
            if (lastChild != null) {
                setProperty(memory, AnalyzerCommons.Identifiers.Value, lastChild)
            }

            return
        }

        val previous = current.getLeftSibling(memory, toWrite = false)

        if (previous != null) {
            setProperty(memory, AnalyzerCommons.Identifiers.Value, previous)
        }
    }

    /**
     * Inserts a list of children in this position.
     */
    fun insertChild(memory: IMemory, child: LxmNode, isForward: Boolean) {
        val parent = getParent(memory, toWrite = true)
        val current = getCurrent(memory, toWrite = false)
        val previous = current?.getLeftSibling(memory, toWrite = true) ?: parent.getLastChild(memory, toWrite = true)

        parent.insertChild(memory, child, previous)

        // Update index.
        if (!isForward) {
            setProperty(memory, AnalyzerCommons.Identifiers.Value, child)
        }
    }

    /**
     * Clones the current [LxmFilterPosition]
     */
    fun saveCopy(memory: IMemory): LxmFilterPosition {
        val result = LxmFilterPosition(memory, getParent(memory, toWrite = false))

        val value = getCurrent(memory, toWrite = false) ?: return result
        result.setProperty(memory, AnalyzerCommons.Identifiers.Value, value)

        return result
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(memory: IMemory) = LxmFilterPosition(memory, oldVersion = this)

    override fun toString() = "[Filter position] - ${super.toString()}"
}
