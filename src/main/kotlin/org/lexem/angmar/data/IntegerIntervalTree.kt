package org.lexem.angmar.data


/**
 * An integer interval implemented as a Binary Search Tree to increase performance.
 */
class IntegerIntervalTree private constructor() : Iterable<Int> {
    private var root: BstNode? = null

    /**
     * Whether this [IntegerIntervalTree] is empty or not.
     */
    val isEmpty get() = root == null

    /**
     * The size of the tree.
     */
    val size get() = root?.size ?: 0

    // METHODS ----------------------------------------------------------------

    /**
     * Add a point to the current [IntegerIntervalTree].
     */
    fun add(point: Int) {
        root = root?.addRecursive(point) ?: BstNode(IntegerRange.new(point), left = null, right = null, size = 1)
    }

    /**
     * Removes a point from the current [IntegerIntervalTree].
     */
    fun remove(point: Int) {
        root = root?.removeRecursive(point)
    }

    /**
     * Checks whether a point is inside this [IntegerIntervalTree].
     */
    operator fun contains(point: Int): Boolean {
        var node = root
        while (node != null) {
            node = when {
                point < node.range.from -> node.left
                node.range.to < point -> node.right
                else -> return true
            }
        }

        return false
    }

    // INHERIT METHODS --------------------------------------------------------

    /**
     * [IntegerIntervalTree]'s iterator over its points.
     */
    override fun iterator() = sequence {
        root?.let { yieldAll(it.iterator()) }
    }.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntegerIntervalTree

        if (root != other.root) return false

        return true
    }

    override fun hashCode(): Int {
        return root.hashCode()
    }

    override fun toString() = "[${root ?: ""}]"

    // STATIC -----------------------------------------------------------------

    /**
     * [IntegerIntervalTree]'s bst nodes.
     */
    private data class BstNode(var range: IntegerRange, var left: BstNode?, var right: BstNode?, var size: Int) :
            Iterable<Int> {

        /**
         * The height of the node.
         */
        val height get() = Int.SIZE_BITS - Integer.numberOfLeadingZeros(size) - 1

        // METHODS ------------------------------------------------------------

        /**
         * Adds a point recursively.
         */
        fun addRecursive(point: Int): BstNode? {
            if (point < range.from - 1) {
                left = left?.addRecursive(point) ?: BstNode(IntegerRange.new(point), left = null, right = null,
                        size = 1)
                return this
            }

            if (range.to + 1 < point) {
                right = right?.removeRecursive(point) ?: BstNode(IntegerRange.new(point), left = null, right = null,
                        size = 1)
                return this
            }

            when (point) {
                // Case 2.1/3.1: touching left bound
                range.from - 1 -> {
                    if (right == null) {
                        // Case 2.1: modify current range
                        range = IntegerRange.new(point, range.to)
                    } else {
                        val info = OperationInfo()
                        left = left!!.getGreatestAndRemoveIfTouches(point, info)

                        if (info.isRemoved) {
                            // Case 3.1: join two ranges
                            range = IntegerRange.new(info.range!!.from, range.to)
                            // TODO balance
                        } else {
                            // Case 2.1: modify current range
                            range = IntegerRange.new(point, range.to)
                        }
                    }
                }
                // Case 2.2/3.2: touching right bound
                range.to + 1 -> {
                    if (right == null) {
                        // Case 2.2: modify current range
                        range = IntegerRange.new(range.from, point)
                    } else {
                        val info = OperationInfo()
                        right = right!!.getSmallestAndRemoveIfTouches(point, info)

                        if (info.isRemoved) {
                            // Case 3.2: join two ranges
                            range = IntegerRange.new(range.from, info.range!!.to)
                            // TODO balance
                        } else {
                            // Case 2.2: modify current range
                            range = IntegerRange.new(range.from, point)
                        }
                    }
                }
                // Case 1: already inside.
                else -> {
                }
            }

            return this
        }

        /**
         * Removes a point recursively.
         */
        fun removeRecursive(point: Int): BstNode? {
            if (point < range.from) {
                left = left?.removeRecursive(point)
                return this
            }

            if (range.to < point) {
                right = right?.removeRecursive(point)
                return this
            }

            when {
                // Case 4: remove all range - remove node - balance
                range.from == range.to -> {
                    // Substitute the node by one of its children.
                    if (left == null) {
                        // TODO if right == null -> balance
                        return right
                    }

                    if (right == null) {
                        return left
                    }

                    // Get the inorder successor (smallest in the right subtree)
                    val info = OperationInfo()
                    right = right!!.getSmallestAndRemove(info)
                    range = info.range!!

                    // TODO balance
                }
                // Case 2.1: left bound
                range.from == point -> {
                    range = IntegerRange.new(range.from + 1, range.to)
                }
                // Case 2.2: right bound
                range.to == point -> {
                    range = IntegerRange.new(range.from, range.to - 1)
                }
                // Case 3: middle - duplicate node - balance
                else -> {
                    val newLeftRange = IntegerRange.new(range.from, point - 1)
                    val newRightRange = IntegerRange.new(point + 1, range.to)

                    if (left == null) {
                        left = BstNode(newLeftRange, left = null, right = null, size = 1)
                        range = newRightRange
                        // TODO if right == null -> balance
                        return this
                    }

                    if (right == null) {
                        right = BstNode(newRightRange, left = null, right = null, size = 1)
                        range = newLeftRange
                        return this
                    }

                    right = BstNode(newRightRange, left = null, right = right, size = right!!.size + 1)
                    range = newLeftRange

                    // TODO balance
                }
            }

            return this
        }

        /**
         * Gets the smallest [IntegerRange] of the tree and removes it.
         */
        private fun getSmallestAndRemove(info: OperationInfo): BstNode? {
            if (left != null) {
                left = left!!.getSmallestAndRemove(info)
                size -= 1
                return this
            }

            info.range = range
            return right
        }

        /**
         * Gets the smallest [IntegerRange] of the tree and removes it if touches the point.
         */
        private fun getSmallestAndRemoveIfTouches(point: Int, info: OperationInfo): BstNode? {
            if (left != null) {
                left = left!!.getSmallestAndRemoveIfTouches(point, info)

                if (info.isRemoved) {
                    size -= 1
                }

                return this
            }

            // Check touch.
            if (range.from - 1 <= point && point <= range.to + 1) {
                info.range = range
                return right
            }

            return this
        }

        /**
         * Gets the greatest [IntegerRange] of the tree and removes it if touches the point.
         */
        private fun getGreatestAndRemoveIfTouches(point: Int, info: OperationInfo): BstNode? {
            if (right != null) {
                right = right!!.getGreatestAndRemoveIfTouches(point, info)

                if (info.isRemoved) {
                    size -= 1
                }

                return this
            }

            // Check touch.
            if (range.from - 1 <= point && point <= range.to + 1) {
                info.range = range
                return left
            }

            return this
        }

        // INHERIT METHODS --------------------------------------------------------

        override fun iterator(): Iterator<Int> = sequence {
            left?.let { yieldAll(it) }
            yieldAll(range.iterator())
            right?.let { yieldAll(it) }
        }.iterator()

        override fun toString() = StringBuilder().apply {
            if (left != null) {
                append(left)
                append(' ')
            }

            if (range.from != range.to) {
                append(range.from)
                append("..")
                append(range.to)
            } else {
                append(range.from)
            }

            if (right != null) {
                append(' ')
                append(right)
            }
        }.toString()

        // STATIC -------------------------------------------------------------

        private data class OperationInfo(var range: IntegerRange? = null) {
            val isRemoved get() = range != null
        }
    }
}
