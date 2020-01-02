package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*

/**
 * The Lexem values of the Node type.
 */
internal class LxmNode : LxmObject {
    val name: String
    val type: LxmNodeType

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: IMemory, name: String, from: IReaderCursor, type: LxmNodeType = LxmNodeType.Custom) : super(
            memory) {
        this.name = name
        this.type = type

        setProperty(memory, AnalyzerCommons.Identifiers.Name, LxmString.from(name), isConstant = true)
        setFrom(memory, from)

        init(memory)
    }

    private constructor(memory: IMemory, oldVersion: LxmNode) : super(memory, oldVersion) {
        name = oldVersion.name
        type = oldVersion.type
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Adds the initial properties.
     */
    private fun init(memory: IMemory) {
        val properties = LxmObject(memory)
        setProperty(memory, AnalyzerCommons.Identifiers.Properties, properties, isConstant = true)

        setProperty(memory, AnalyzerCommons.Identifiers.HiddenChildCount, LxmInteger.Num0, isConstant = true)

        val defaultProperties = AnalyzerCommons.getDefaultPropertiesByType(type)
        for ((key, value) in defaultProperties) {
            properties.setProperty(memory, key, value)
        }
    }

    /**
     * Adds the node to the parent.
     */
    fun addToParent(memory: IMemory, newParent: LxmNode) {
        val parentLastChild = newParent.getLastChild(memory, toWrite = true)
        if (parentLastChild == null) {
            // Link before removing to avoid reference counting errors.
            newParent.setFirstChild(memory, this)
            newParent.setLastChild(memory, this)

            // Remove from old parent.
            removeFromParent(memory)
        } else {
            // Link before removing to avoid reference counting errors.
            parentLastChild.setRightSibling(memory, this)
            newParent.setLastChild(memory, this)

            // Remove from old parent.
            removeFromParent(memory)

            setLeftSibling(memory, parentLastChild)
        }

        // Updates parent reference.
        setParent(memory, newParent)

        // Update children count of parent.
        newParent.changeChildCount(memory, 1)
    }

    /**
     * Adds the node to the parent.
     */
    fun removeFromParent(memory: IMemory) {
        val parent = getParent(memory, toWrite = true) ?: return
        val parentChildCount = parent.getChildCount(memory)

        // Decrease count.
        parent.changeChildCount(memory, -1)

        when {
            parentChildCount == 1 -> {
                parent.setFirstChild(memory, null)
                parent.setLastChild(memory, null)
            }
            parentChildCount > 1 -> {
                val parentFirstChild = parent.getFirstChild(memory, toWrite = false)!!
                val parentLastChild = parent.getLastChild(memory, toWrite = false)!!

                // First child.
                when {
                    RelationalFunctions.identityEquals(parentFirstChild, this) -> {
                        val rightSibling = getRightSibling(memory, toWrite = true)!!

                        parent.setFirstChild(memory, rightSibling)
                        rightSibling.setLeftSibling(memory, null)
                    }
                    // Last child.
                    RelationalFunctions.identityEquals(parentLastChild, this) -> {
                        val leftSibling = getLeftSibling(memory, toWrite = true)!!
                        parent.setLastChild(memory, leftSibling)
                        leftSibling.setRightSibling(memory, null)
                    }
                    // Middle child.
                    else -> {
                        val leftSibling = getLeftSibling(memory, toWrite = true)!!
                        val rightSibling = getRightSibling(memory, toWrite = true)!!

                        leftSibling.setRightSibling(memory, rightSibling)
                        rightSibling.setLeftSibling(memory, leftSibling)
                    }
                }
            }
            else -> throw AngmarUnreachableException()
        }

        setParent(memory, null)
        setLeftSibling(memory, null)
        setRightSibling(memory, null)
    }

    /**
     * Adds a node after the specified node.
     */
    fun insertChild(memory: IMemory, child: LxmNode, after: LxmNode?) {
        if (after != null && !RelationalFunctions.identityEquals(after.getParent(memory, toWrite = false) ?: LxmNil,
                        this)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectNodeReference,
                    "Cannot insert a list of nodes after a node whenever the node does not belong to the current one") {}
        }

        // Insert at the beginning.
        if (after == null) {
            val firstChild = getFirstChild(memory, toWrite = true)

            // Empty
            if (firstChild == null) {
                // Link before removing to avoid reference counting errors.
                setFirstChild(memory, child)
                setLastChild(memory, child)

                // Remove from old parent.
                child.removeFromParent(memory)
            } else {
                // Link before removing to avoid reference counting errors.
                setFirstChild(memory, child)
                firstChild.setLeftSibling(memory, child)

                // Remove from old parent.
                child.removeFromParent(memory)

                child.setRightSibling(memory, firstChild)
            }
        } else {
            val afterRightSibling = after.getRightSibling(memory, toWrite = true)

            // Insert at the end.
            if (afterRightSibling == null) {
                // Link before removing to avoid reference counting errors.
                setLastChild(memory, child)
                after.setRightSibling(memory, child)

                // Remove from old parent.
                child.removeFromParent(memory)

                child.setLeftSibling(memory, after)
            }
            // Insert in the middle.
            else {
                // Link before removing to avoid reference counting errors.
                after.setRightSibling(memory, child)
                afterRightSibling.setLeftSibling(memory, child)

                // Remove from old parent.
                child.removeFromParent(memory)

                child.setLeftSibling(memory, after)
                child.setRightSibling(memory, afterRightSibling)
            }
        }

        // Updates parent reference.
        child.setParent(memory, this)

        // Update children count.
        changeChildCount(memory, 1)
    }

    /**
     * Moves all the children of this node to its parent.
     */
    fun replaceNodeByItsChildren(memory: IMemory) {
        val parent = getParent(memory, toWrite = true) ?: return
        val firstChild = getFirstChild(memory, toWrite = true) ?: let {
            // No children.
            removeFromParent(memory)
            return
        }
        val lastChild = getFirstChild(memory, toWrite = true)!!
        val leftSibling = getLeftSibling(memory, toWrite = true)
        val rightSibling = getRightSibling(memory, toWrite = true)

        // Update the sibling references.
        if (leftSibling == null) {
            parent.setFirstChild(memory, firstChild)
        } else {
            leftSibling.setRightSibling(memory, firstChild)
        }

        if (rightSibling == null) {
            parent.setLastChild(memory, lastChild)
        } else {
            rightSibling.setLeftSibling(memory, lastChild)
        }

        // Update the children count in the parent.
        parent.changeChildCount(memory, getChildCount(memory) - 1)

        // Update the parent indexes of the children.
        var node: LxmNode? = firstChild
        while (node != null) {
            node.setParent(memory, parent)

            node = node.getRightSibling(memory, toWrite = true)
        }

        // Clears the children.
        clearChildren(memory)
    }

    /**
     * Clears the children of the node.
     */
    fun clearChildren(memory: IMemory) {
        setChildCount(memory, 0)
        setFirstChild(memory, null)
        setLastChild(memory, null)
    }

    /**
     * Gets the child count.
     */
    fun getChildCount(memory: IMemory) =
            (getPropertyValue(memory, AnalyzerCommons.Identifiers.HiddenChildCount) as LxmInteger).primitive

    /**
     * Changes the child count.
     */
    private fun changeChildCount(memory: IMemory, by: Int) {
        val childCount = getChildCount(memory) + by
        if (childCount < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectNodeChildCount,
                    "The child count of a node is incorrect") {}
        }

        setProperty(memory, AnalyzerCommons.Identifiers.HiddenChildCount, LxmInteger.from(childCount),
                isConstant = true, ignoreConstant = true)
    }

    /**
     * Sets the child count.
     */
    private fun setChildCount(memory: IMemory, newValue: Int) {
        if (newValue < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectNodeChildCount,
                    "The child count of a node is incorrect") {}
        }

        setProperty(memory, AnalyzerCommons.Identifiers.HiddenChildCount, LxmInteger.from(newValue), isConstant = true,
                ignoreConstant = true)
    }

    /**
     * Gets the parent node reference.
     */
    fun getParentReference(memory: IMemory) =
            getPropertyValue(memory, AnalyzerCommons.Identifiers.Parent) as? LxmReference

    /**
     * Gets the parent node.
     */
    fun getParent(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.Parent, toWrite)

    /**
     * Sets the parent node.
     */
    private fun setParent(memory: IMemory, parent: LxmNode?) = if (parent == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.Parent, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.Parent, parent, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the first child.
     */
    fun getFirstChild(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.FirstChild, toWrite)

    /**
     * Sets the first child.
     */
    private fun setFirstChild(memory: IMemory, child: LxmNode?) = if (child == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.FirstChild, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.FirstChild, child, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the last child.
     */
    fun getLastChild(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.LastChild, toWrite)

    /**
     * Sets the last child.
     */
    private fun setLastChild(memory: IMemory, child: LxmNode?) = if (child == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.LastChild, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.LastChild, child, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the right sibling.
     */
    fun getRightSibling(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.RightSibling, toWrite)

    /**
     * Sets the right sibling.
     */
    private fun setRightSibling(memory: IMemory, sibling: LxmNode?) = if (sibling == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.RightSibling, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.RightSibling, sibling, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the left sibling.
     */
    fun getLeftSibling(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.LeftSibling, toWrite)

    /**
     * Sets the left sibling.
     */
    private fun setLeftSibling(memory: IMemory, sibling: LxmNode?) = if (sibling == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.LeftSibling, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.LeftSibling, sibling, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the property object.
     */
    fun getProperties(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.Properties, toWrite)!!

    /**
     * Gets the initial position of the content of the node.
     */
    fun getFrom(memory: IMemory) = getPropertyValue(memory, AnalyzerCommons.Identifiers.HiddenFrom) as LxmReaderCursor

    /**
     * Sets the value of the from property.
     */
    private fun setFrom(memory: IMemory, cursor: IReaderCursor) {
        setProperty(memory, AnalyzerCommons.Identifiers.From, LxmInteger.from(cursor.position()), isConstant = true,
                ignoreConstant = true)
        setProperty(memory, AnalyzerCommons.Identifiers.HiddenFrom, LxmReaderCursor(cursor), isConstant = true,
                ignoreConstant = true)
    }

    /**
     * Gets the final position of the content of the node.
     */
    fun getTo(memory: IMemory) = getPropertyValue(memory, AnalyzerCommons.Identifiers.HiddenTo) as? LxmReaderCursor

    /**
     * Sets the value of the to property.
     */
    fun setTo(memory: IMemory, cursor: IReaderCursor) {
        setProperty(memory, AnalyzerCommons.Identifiers.To, LxmInteger.from(cursor.position()), isConstant = true,
                ignoreConstant = true)
        setProperty(memory, AnalyzerCommons.Identifiers.HiddenTo, LxmReaderCursor(cursor), isConstant = true,
                ignoreConstant = true)
    }

    /**
     * Gets the children as a list.
     */
    fun getChildrenList(memory: IMemory, toWrite: Boolean) = sequence {
        var child = getFirstChild(memory, toWrite)
        while (child != null) {
            yield(child!!)

            child = child.getRightSibling(memory, toWrite)
        }
    }

    /**
     * Gets the index in the parent node.
     */
    fun getParentIndex(memory: IMemory): Int {
        val parent = getParent(memory, toWrite = false) ?: return -1
        for ((index, child) in parent.getChildrenList(memory, toWrite = false).withIndex()) {
            if (RelationalFunctions.identityEquals(this, child)) {
                return index
            }
        }

        return -1
    }

    /**
     * Gets the content of the node.
     */
    fun getContent(memory: IMemory): LexemPrimitive? {
        val from = getFrom(memory).primitive
        val to = getTo(memory)?.primitive ?: return null
        val reader = to.getReader()

        return AnalyzerCommons.substringReader(reader, from, to)
    }

    /**
     * Applies an offset to the current node and its children.
     */
    fun applyOffset(memory: IMemory, offset: IReaderCursor) {
        val newReader = offset.getReader()
        val offsetAsInt = offset.position()

        // Apply to all nodes.
        var node = this
        outer@ while (true) {
            // Apply to the current node.
            val from = node.getFrom(memory).primitive.position()
            val to = node.getTo(memory)!!.primitive.position()

            node.setFrom(memory, newReader.saveCursorAt(offsetAsInt + from)!!)
            node.setTo(memory, newReader.saveCursorAt(offsetAsInt + to)!!)

            // Get next child node.
            val firstChildNode = node.getFirstChild(memory, toWrite = true)
            if (firstChildNode != null) {
                node = firstChildNode
                continue
            }

            // Get the next right sibling node.
            var parentNode = node
            while (parentNode != this) {
                val rightSiblingNode = parentNode.getRightSibling(memory, toWrite = true)
                if (rightSiblingNode != null) {
                    node = rightSiblingNode
                    continue@outer
                }

                parentNode = parentNode.getParent(memory, toWrite = false)!!
            }

            break
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(memory: IMemory) = LxmNode(memory, this)

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, NodeType.TypeName) as LxmReference
    }

    override fun toString() = "[Node] $name = ${super.toString()}"

    // STATIC -----------------------------------------------------------------

    /**
     * The types of contexts.
     */
    enum class LxmNodeType {
        Root,
        Expression,
        ExpressionGroup,
        Filter,
        FilterGroup,
        Custom,
    }
}
