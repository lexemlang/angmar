package org.lexem.angmar.parser

import java.util.*

/**
 * Keeps a reference to already processed [ParserNode] by its start position.
 */
internal class ForwardBuffer {
    private var tree = TreeMap<Int, MutableMap<Class<Any>, ParserNode>>()

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a new [ParserNode] to the [ForwardBuffer].
     */
    fun <T : ParserNode> add(node: T) {
        val map = tree[node.from.position()] ?: let {
            val newMap = hashMapOf<Class<Any>, ParserNode>()
            tree[node.from.position()] = newMap
            newMap
        }

        val clazz: Class<Any> = node.javaClass
        map[clazz] = node
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
    @Suppress("UNCHECKED_CAST")
    fun <T : ParserNode> find(position: Int, type: Class<T>): T? {
        val map = tree[position] ?: return null
        val res = map[type as Class<Any>] ?: return null
        return res as T
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
