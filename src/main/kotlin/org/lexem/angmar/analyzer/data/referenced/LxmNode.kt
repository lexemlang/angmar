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

    constructor(memory: LexemMemory, name: String, from: IReaderCursor, type: LxmNodeType = LxmNodeType.Custom) : super(
            memory) {
        this.name = name
        this.type = type

        setProperty(memory, AnalyzerCommons.Identifiers.Name, LxmString.from(name), isConstant = true)
        setFrom(memory, from)

        init(memory)
    }

    private constructor(memory: LexemMemory, oldVersion: LxmNode) : super(memory, oldVersion, true) {
        name = oldVersion.name
        type = oldVersion.type
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Adds the initial properties.
     */
    private fun init(memory: LexemMemory) {
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
    fun addToParent(memory: LexemMemory, parent: LxmNode) {
        // Remove from previous parent.
        removeFromParent(memory)

        parent.changeChildCount(memory, 1)
        val parentLastChild = parent.getLastChild(memory, toWrite = true)

        if (parentLastChild == null) {
            parent.setFirstChild(memory, this)
            parent.setLastChild(memory, this)
        } else {
            parentLastChild.setRightSibling(memory, this)
            setLeftSibling(memory, parentLastChild)
            parent.setLastChild(memory, this)
        }
    }

    /**
     * Adds the node to the parent.
     */
    fun removeFromParent(memory: LexemMemory) {
        val parent = getParent(memory, toWrite = true) ?: return

        // Decrease count.
        parent.changeChildCount(memory, -1)

        val parentFirstChild = parent.getFirstChild(memory, toWrite = false)!!
        val parentLastChild = parent.getLastChild(memory, toWrite = false)!!

        // First child.
        if (RelationalFunctions.identityEquals(parentFirstChild, this)) {
            // First and Last child.
            if (RelationalFunctions.identityEquals(parentLastChild, this)) {
                parent.setFirstChild(memory, null)
                parent.setLastChild(memory, null)
            }
            // First child.
            else {
                val rightSibling = getRightSibling(memory, toWrite = true)!!

                parent.setFirstChild(memory, rightSibling)
                rightSibling.setLeftSibling(memory, null)
            }
        }
        // Last child.
        else if (RelationalFunctions.identityEquals(parentLastChild, this)) {
            val leftSibling = getLeftSibling(memory, toWrite = true)!!
            parent.setLastChild(memory, leftSibling)
            leftSibling.setRightSibling(memory, null)
        }
        // Middle child.
        else {
            val leftSibling = getLeftSibling(memory, toWrite = true)!!
            val rightSibling = getRightSibling(memory, toWrite = true)!!

            leftSibling.setRightSibling(memory, rightSibling)
            rightSibling.setLeftSibling(memory, leftSibling)
        }

        setParent(memory, null)
        setLeftSibling(memory, null)
        setRightSibling(memory, null)
    }

    /**
     * Adds a list of nodes after the specified node.
     */
    fun insertChildren(memory: LexemMemory, children: List<LxmNode>, after: LxmNode?) {
        if (children.isEmpty()) {
            return
        }

        if (after != null && !RelationalFunctions.identityEquals(after.getParent(memory, toWrite = false) ?: LxmNil,
                        this)) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectNodeReference,
                    "Cannot insert a list of nodes after a node whenever the node does not belong to the current one") {}
        }

        // Remove from previously parent and set new parent.
        let {
            var previousNode: LxmNode? = null
            for (node in children) {
                node.removeFromParent(memory)

                node.setParent(memory, this)
                node.setLeftSibling(memory, previousNode)
                previousNode?.setRightSibling(memory, node)

                previousNode = node
            }
        }

        // Insert after the node.
        if (after == null) {
            val firstChild = getFirstChild(memory, toWrite = true)

            if (firstChild == null) {
                setFirstChild(memory, children.first())
                setLastChild(memory, children.last())
            } else {
                setFirstChild(memory, children.first())
                firstChild.setLeftSibling(memory, children.last())
            }
        } else {
            val rightSibling = after.getRightSibling(memory, toWrite = true)

            if (rightSibling == null) {
                after.setRightSibling(memory, children.first())
                setLastChild(memory, children.last())
            } else {
                after.setRightSibling(memory, children.first())
                rightSibling.setLeftSibling(memory, children.last())
            }
        }

        changeChildCount(memory, children.size)
    }

    /**
     * Moves all the children of this node to its parent.
     */
    fun replaceNodeInParentByChildren(memory: LexemMemory) {
        val parent = getParent(memory, toWrite = true) ?: return
        val leftSibling = getLeftSibling(memory, toWrite = true)
        val children = getChildrenList(memory, toWrite = true)

        parent.insertChildren(memory, children.toList(), leftSibling)

        removeFromParent(memory)
    }

    /**
     * Clears the children of the node.
     */
    fun clearChildren(memory: LexemMemory) {
        val childCount = getChildCount(memory)
        changeChildCount(memory, -childCount)
        setFirstChild(memory, null)
        setLastChild(memory, null)
    }

    /**
     * Gets the child count.
     */
    fun getChildCount(memory: LexemMemory) =
            (getPropertyValue(memory, AnalyzerCommons.Identifiers.HiddenChildCount) as LxmInteger).primitive

    /**
     * Changes the child count.
     */
    private fun changeChildCount(memory: LexemMemory, by: Int) {
        val childCount = getChildCount(memory) + by
        if (childCount < 0) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectNodeChildCount,
                    "The child count of a node is incorrect") {}
        }

        setProperty(memory, AnalyzerCommons.Identifiers.HiddenChildCount, LxmInteger.from(childCount),
                isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the parent node reference.
     */
    fun getParentReference(memory: LexemMemory) =
            getPropertyValue(memory, AnalyzerCommons.Identifiers.Parent) as? LxmReference

    /**
     * Gets the parent node.
     */
    fun getParent(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.Parent, toWrite)

    /**
     * Sets the parent node.
     */
    private fun setParent(memory: LexemMemory, parent: LxmNode?) = if (parent == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.Parent, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.Parent, parent, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the first child.
     */
    fun getFirstChild(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.FirstChild, toWrite)

    /**
     * Sets the first child.
     */
    private fun setFirstChild(memory: LexemMemory, child: LxmNode?) = if (child == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.FirstChild, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.FirstChild, child, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the last child.
     */
    fun getLastChild(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.LastChild, toWrite)

    /**
     * Sets the last child.
     */
    private fun setLastChild(memory: LexemMemory, child: LxmNode?) = if (child == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.LastChild, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.LastChild, child, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the right sibling.
     */
    fun getRightSibling(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.RightSibling, toWrite)

    /**
     * Sets the right sibling.
     */
    private fun setRightSibling(memory: LexemMemory, sibling: LxmNode?) = if (sibling == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.RightSibling, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.RightSibling, sibling, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the left sibling.
     */
    fun getLeftSibling(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmNode>(memory, AnalyzerCommons.Identifiers.LeftSibling, toWrite)

    /**
     * Sets the left sibling.
     */
    private fun setLeftSibling(memory: LexemMemory, sibling: LxmNode?) = if (sibling == null) {
        removeProperty(memory, AnalyzerCommons.Identifiers.LeftSibling, ignoreConstant = true)
    } else {
        setProperty(memory, AnalyzerCommons.Identifiers.LeftSibling, sibling, isConstant = true, ignoreConstant = true)
    }

    /**
     * Gets the property object.
     */
    fun getProperties(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.Properties, toWrite)!!

    /**
     * Gets the initial position of the content of the node.
     */
    fun getFrom(memory: LexemMemory) =
            getPropertyValue(memory, AnalyzerCommons.Identifiers.HiddenFrom) as LxmReaderCursor

    /**
     * Sets the value of the from property.
     */
    private fun setFrom(memory: LexemMemory, cursor: IReaderCursor) {
        setProperty(memory, AnalyzerCommons.Identifiers.From, LxmInteger.from(cursor.position()), isConstant = true,
                ignoreConstant = true)
        setProperty(memory, AnalyzerCommons.Identifiers.HiddenFrom, LxmReaderCursor(cursor), isConstant = true,
                ignoreConstant = true)
    }

    /**
     * Gets the final position of the content of the node.
     */
    fun getTo(memory: LexemMemory) = getPropertyValue(memory, AnalyzerCommons.Identifiers.HiddenTo) as? LxmReaderCursor

    /**
     * Sets the value of the to property.
     */
    fun setTo(memory: LexemMemory, cursor: IReaderCursor) {
        setProperty(memory, AnalyzerCommons.Identifiers.To, LxmInteger.from(cursor.position()), isConstant = true,
                ignoreConstant = true)
        setProperty(memory, AnalyzerCommons.Identifiers.HiddenTo, LxmReaderCursor(cursor), isConstant = true,
                ignoreConstant = true)
    }

    /**
     * Gets the children as a list.
     */
    fun getChildrenList(memory: LexemMemory, toWrite: Boolean) = sequence {
        var child = getFirstChild(memory, toWrite)
        while (child != null) {
            yield(child!!)

            child = child.getRightSibling(memory, toWrite)
        }
    }

    /**
     * Gets the index in the parent node.
     */
    fun getParentIndex(memory: LexemMemory): Int {
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
    fun getContent(memory: LexemMemory): LexemPrimitive? {
        val from = getFrom(memory).primitive
        val to = getTo(memory)?.primitive ?: return null
        val reader = to.getReader()

        return AnalyzerCommons.substringReader(reader, from, to)
    }

    /**
     * Applies an offset to the current node and its children.
     */
    fun applyOffset(memory: LexemMemory, offset: IReaderCursor) {
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

    override fun memoryShift(memory: LexemMemory) = LxmNode(memory, oldVersion = this)

    override fun getType(memory: LexemMemory): LxmReference {
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
