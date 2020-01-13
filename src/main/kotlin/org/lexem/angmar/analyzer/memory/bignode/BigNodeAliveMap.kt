package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.data.*

/**
 * The representation of a stack of alive [BigNode]s.
 */
internal class BigNodeAliveMap {
    private val alive = hashSetOf<Int>()
    private val collapsed = hashSetOf<Int>()
    private var maximum = -1
    private val synchronizer = WriteFirstSynchronizer()

    // METHODS ----------------------------------------------------------------

    /**
     * Checks whether a [BigNode] is alive or collapsed.
     */
    fun isAliveOrCollapsed(memory: IMemory, id: Int): Boolean {
        val currentId = memory.getBigNodeId()

        // Remove those that are not used.
        clearLeftOver(currentId + 1)

        return synchronizer.syncLetToRead { id in alive || id in collapsed }
    }

    /**
     * Checks whether a [BigNode] is alive.
     */
    fun isAlive(memory: IMemory, id: Int): Boolean {
        val currentId = memory.getBigNodeId()

        // Remove those that are not used.
        clearLeftOver(currentId + 1)

        return synchronizer.syncLetToRead { id in alive }
    }

    /**
     * Checks whether a [BigNode] is collapsed.
     */
    fun isCollapsed(memory: IMemory, id: Int): Boolean {
        val currentId = memory.getBigNodeId()

        // Remove those that are not used.
        clearLeftOver(currentId + 1)

        return synchronizer.syncLetToRead { id in collapsed }
    }

    /**
     * Checks whether a [BigNode] is removed.
     */
    fun isRemoved(memory: IMemory, id: Int) = !isAliveOrCollapsed(memory, id)

    /**
     * Sets a [BigNode] as alive.
     */
    fun setAlive(id: Int) {
        synchronizer.syncToWrite {
            alive.add(id)
            collapsed.remove(id)
        }

        maximum = maxOf(id, maximum)
    }

    /**
     * Sets a [BigNode] as collapsed.
     */
    fun setCollapsed(id: Int) {
        synchronizer.syncToWrite {
            alive.remove(id)
            collapsed.add(id)
        }
    }

    /**
     * Sets a [BigNode] as dead and the following.
     */
    fun setDeadFrom(memory: IMemory, id: Int) {
        synchronizer.syncToWrite {
            alive.remove(id)
            collapsed.remove(id)
        }

        // Destroy the rest collapsed.
        val newMaximum = minOf(memory.getBigNodeId() + 1, id)
        clearLeftOver(newMaximum)

        // Sets the previous.
        synchronizer.syncToWrite {
            maximum = newMaximum - 1
        }
    }

    /**
     * Removes those leftover [BigNode]'s.
     */
    private fun clearLeftOver(id: Int) {
        if (id <= maximum) {
            synchronizer.syncToWrite {
                for (i in id..maximum) {
                    alive.remove(i)
                    collapsed.remove(i)
                }

                maximum = id - 1
            }
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "Alive: ${alive.size} Collapsed: ${collapsed.size}"
}

