package org.lexem.angmar.nodes

import java.util.*

/**
 * Keeps a reference to already processed [ParserNode] by its start position.
 */
class ForwardBuffer {
    private var tree = TreeMap<Int, MutableMap<NodeType, ParserNode>>()

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new [ParserNode] to the [ForwardBuffer].
     */
    fun add(node: ParserNode) {
        val map = tree[node.from.position()] ?: let {
            val newMap = mutableMapOf<NodeType, ParserNode>()
            tree[node.from.position()] = newMap
            newMap
        }

        map[node.type] = node
    }

    /**
     * Removes the specified positions.
     */
    fun remove(position: Int) {
        tree.remove(position)
    }

    /**
     * Removes a range of positions.
     */
    fun remove(from: Int, to: Int) {
        var current = tree.ceilingKey(from) ?: return
        while (current <= to) {
            tree.remove(current)
            current = tree.ceilingKey(from) ?: return
        }
    }

    /**
     * Find a [ParserNode] by position and type in the buffer.
     */
    fun find(position: Int, type: NodeType): ParserNode? {
        val map = tree[position] ?: return null
        return map[type]
    }

    /**
     * Clears the buffer to remove any reference to its values.
     */
    fun clear() {
        for (keyMap in tree) {
            keyMap.value.clear()
        }

        tree.clear()
    }
}