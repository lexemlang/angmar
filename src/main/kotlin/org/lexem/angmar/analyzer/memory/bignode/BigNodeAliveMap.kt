package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.memory.*

/**
 * The representation of a stack of alive [BigNode]s.
 */
internal class BigNodeAliveMap {
    private val alive = hashSetOf<Int>()
    private val collapsed = hashMapOf<Int, Int>()
    private var maximum = -1

    // METHODS ----------------------------------------------------------------

    /**
     * Checks whether a [BigNode] is alive.
     */
    fun isAlive(memory: IMemory, id: Int): Boolean {
        val currentId = memory.getBigNodeId()

        // Remove those that are not used.
        clearLeftOver(currentId + 1)

        return id in alive || id in collapsed
    }

    /**
     * Sets a [BigNode] as alive.
     */
    fun setAlive(id: Int) {
        alive.add(id)
        collapsed.remove(id)

        maximum = maxOf(id, maximum)
    }

    /**
     * Sets a [BigNode] as collapsed.
     */
    fun setCollapsed(id: Int, to: Int) {
        alive.remove(id)
        collapsed[id] = to
    }

    /**
     * Sets a [BigNode] as dead and the following.
     */
    fun setDeadFrom(memory: IMemory, id: Int) {
        alive.remove(id)
        collapsed.remove(id)

        // Destroy the rest collapsed.
        val newMaximum = minOf(memory.getBigNodeId() + 1, id)
        clearLeftOver(newMaximum)

        // Sets the previous.
        maximum = newMaximum - 1
    }

    /**
     * Removes those leftover [BigNode]'s.
     */
    private fun clearLeftOver(id: Int) {
        if (id <= maximum) {
            for (i in id..maximum) {
                alive.remove(i)
                collapsed.remove(i)
            }

            maximum = id - 1
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "Alive: ${alive.size} Collapsed: ${collapsed.size}"
}

