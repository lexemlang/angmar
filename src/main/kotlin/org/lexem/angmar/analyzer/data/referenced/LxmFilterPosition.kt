package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem value for a pattern union.
 */
internal class LxmFilterPosition : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: IMemory, parent: LxmNode) : super(memory) {
        setProperty(memory, AnalyzerCommons.Identifiers.Parent, parent)
        setProperty(memory, AnalyzerCommons.Identifiers.Index, LxmInteger.Num0)

        if (parent.getChildCount(memory) > 0) {
            setProperty(memory, AnalyzerCommons.Identifiers.Value, parent.getFirstChild(memory, toWrite = false)!!)
        }
    }

    private constructor(memory: IMemory, oldVersion: LxmFilterPosition) : super(memory, oldVersion = oldVersion)

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the index.
     */
    fun getIndex(memory: IMemory) =
            getDereferencedProperty<LxmInteger>(memory, AnalyzerCommons.Identifiers.Index, toWrite = false)!!.primitive

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
    fun getPrevious(memory: IMemory, toWrite: Boolean): LxmNode? {
        val current = getCurrent(memory, toWrite = false)
        if (current != null) {
            return current.getRightSibling(memory, toWrite)
        }

        return getParent(memory, toWrite = false).getLastChild(memory, toWrite)
    }

    /**
     * Advances the index one position.
     */
    fun advance(memory: IMemory) {
        val parent = getParent(memory, toWrite = false)
        val index = getIndex(memory)
        val childCount = parent.getChildCount(memory)

        if (index == childCount) {
            return
        }

        val current = getCurrent(memory, toWrite = false)!!
        setProperty(memory, AnalyzerCommons.Identifiers.Index, LxmInteger.from(index + 1))

        if (index == childCount - 1) {
            removeProperty(memory, AnalyzerCommons.Identifiers.Value)
        } else {
            setProperty(memory, AnalyzerCommons.Identifiers.Value, current.getRightSibling(memory, toWrite = false)!!)
        }
    }

    /**
     * Advances backwards the index one position.
     */
    fun advanceBack(memory: IMemory) {
        val index = getIndex(memory)

        if (index == 0) {
            return
        }

        val current = getCurrent(memory, toWrite = false)!!
        setProperty(memory, AnalyzerCommons.Identifiers.Index, LxmInteger.from(index - 1))
        setProperty(memory, AnalyzerCommons.Identifiers.Value, current.getLeftSibling(memory, toWrite = false)!!)
    }

    /**
     * Inserts a list of children in this position.
     */
    fun insertChild(memory: IMemory, child: LxmNode, isForward: Boolean) {
        val parent = getParent(memory, toWrite = true)
        val current = getCurrent(memory, toWrite = false)
        val previous = if (current != null) {
            current.getLeftSibling(memory, toWrite = true)
        } else {
            parent.getLastChild(memory, toWrite = true)
        }

        parent.insertChild(memory, child, previous)

        // Update index.
        if (isForward) {
            val index = getIndex(memory)
            setProperty(memory, AnalyzerCommons.Identifiers.Index, LxmInteger.from(index + 1))
        } else {
            setProperty(memory, AnalyzerCommons.Identifiers.Value, child)
        }

    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(memory: IMemory) = LxmFilterPosition(memory, oldVersion = this)

    override fun toString() = "[Filter position] - ${super.toString()}"
}
