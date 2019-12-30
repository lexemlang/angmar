package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.io.*
import java.util.*

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

        setProperty(AnalyzerCommons.Identifiers.Name, LxmString.from(name), isConstant = true)
        setFrom(from)
        setParentIndex(-1)

        init(memory)
    }

    private constructor(bigNode: BigNode, oldVersion: LxmNode) : super(bigNode, oldVersion) {
        name = oldVersion.name
        type = oldVersion.type
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Adds the initial properties.
     */
    private fun init(memory: LexemMemory) {
        val children = LxmList(memory)
        children.makeConstant()
        setProperty(AnalyzerCommons.Identifiers.Children, children, isConstant = true)

        val properties = LxmObject(memory)
        setProperty(AnalyzerCommons.Identifiers.Properties, properties, isConstant = true)

        val defaultProperties = AnalyzerCommons.getDefaultPropertiesByType(type)
        for ((key, value) in defaultProperties) {
            properties.setProperty(key, value)
        }
    }

    /**
     * Adds the node to the parent.
     */
    fun addToParent(parent: LxmNode) {
        // Remove from previously parent.
        removeFromParent()

        val parentList = parent.getChildren(toWrite = true)

        setParentIndex(parentList.size)
        setProperty(AnalyzerCommons.Identifiers.Parent, parent, isConstant = true, ignoreConstant = true)
        parentList.addCell(this, ignoreConstant = true)
    }

    /**
     * Adds the node to the parent.
     */
    fun removeFromParent() {
        val parent = getParent(toWrite = false) ?: return
        val parentList = parent.getChildren(toWrite = true)
        val parentIndex = getParentIndex()

        for (i in parentIndex + 1 until parentList.size) {
            val node = parentList.getDereferencedCell<LxmNode>(i, toWrite = true)!!
            node.setParentIndex(node.getParentIndex() - 1)
        }

        parentList.removeCell(parentIndex, ignoreConstant = true)

        setParentIndex(-1)
        removeProperty(AnalyzerCommons.Identifiers.Parent, ignoreConstant = true)
    }

    /**
     * Adds a list of nodes at the specified position.
     */
    fun insertChildren(children: List<LxmNode>, at: Int) {
        // Remove from previously parent and set new parent.
        for (node in children) {
            node.removeFromParent()

            node.setParent(this)
        }

        val childList = getChildren(toWrite = true)
        childList.insertCell(at, *children.toTypedArray(), ignoreConstant = true)

        for (i in at until childList.size) {
            val node = childList.getDereferencedCell<LxmNode>(i, toWrite = true)!!
            node.setParentIndex(i)
        }
    }

    /**
     * Moves all the children of this node to its parent.
     */
    fun replaceNodeInParentByChildren() {
        val parent = getParent(toWrite = false) ?: return
        val parentList = parent.getChildren(toWrite = true)
        val childList = getChildrenAsList().map { it.dereference(bigNode, toWrite = false) as LxmNode }
        val parentIndex = getParentIndex()

        // Set the new parent and indexes.
        for ((i, node) in childList.withIndex()) {
            node.setParent(parent)
            node.setParentIndex(parentIndex + i)
        }

        for (index in parentIndex + 1 until parentList.size) {
            val node = parentList.getCell(index)!!.dereference(bigNode, toWrite = true) as LxmNode
            node.setParentIndex(index)
        }

        parentList.replaceCell(parentIndex, 1, *childList.toList().toTypedArray(), ignoreConstant = true)
    }

    /**
     * Clears the children of the node.
     */
    fun clearChildren() {
        val childList = getChildren(toWrite = true)

        for (i in 0 until childList.size) {
            val node = childList.getDereferencedCell<LxmNode>(i, toWrite = true)!!
            node.setParentIndex(-1)
            node.removeProperty(AnalyzerCommons.Identifiers.Parent, ignoreConstant = true)
        }

        childList.removeCell(0, childList.size, ignoreConstant = true)
    }

    /**
     * Gets the parent node reference.
     */
    fun getParentReference() = getPropertyValue(AnalyzerCommons.Identifiers.Parent) as? LxmReference

    /**
     * Gets the parent node.
     */
    fun getParent(toWrite: Boolean) = getDereferencedProperty<LxmNode>(AnalyzerCommons.Identifiers.Parent, toWrite)

    /**
     * Sets the parent node.
     */
    fun setParent(parent: LxmNode) =
            setProperty(AnalyzerCommons.Identifiers.Parent, parent, isConstant = true, ignoreConstant = true)

    /**
     * Gets the index in the parent node.
     */
    fun getParentIndex() = (getPropertyValue(AnalyzerCommons.Identifiers.ParentIndex) as LxmInteger).primitive

    /**
     * Sets the index in the parent node.
     */
    fun setParentIndex(index: Int) =
            setProperty(AnalyzerCommons.Identifiers.ParentIndex, LxmInteger.from(index), isConstant = true,
                    ignoreConstant = true)

    /**
     * Gets the children.
     */
    fun getChildren(toWrite: Boolean) =
            getDereferencedProperty<LxmList>(AnalyzerCommons.Identifiers.Children, toWrite)!!

    /**
     * Gets the children property value as a list.
     */
    fun getChildrenAsList() = getChildren(toWrite = false).getAllCells()

    /**
     * Gets the content of the node.
     */
    fun getContent(): LexemPrimitive? {
        val from = getFrom().primitive
        val to = getTo()?.primitive ?: return null
        val reader = to.getReader()

        return AnalyzerCommons.substringReader(reader, from, to)
    }

    /**
     * Gets the property object.
     */
    fun getProperties(toWrite: Boolean) =
            getDereferencedProperty<LxmObject>(AnalyzerCommons.Identifiers.Properties, toWrite)!!

    /**
     * Gets the initial position of the content of the node.
     */
    fun getFrom() = getPropertyValue(AnalyzerCommons.Identifiers.HiddenFrom) as LxmReaderCursor

    /**
     * Sets the value of the from property.
     */
    fun setFrom(cursor: IReaderCursor) {
        setProperty(AnalyzerCommons.Identifiers.From, LxmInteger.from(cursor.position()), isConstant = true,
                ignoreConstant = true)
        setProperty(AnalyzerCommons.Identifiers.HiddenFrom, LxmReaderCursor(cursor), isConstant = true,
                ignoreConstant = true)
    }

    /**
     * Gets the final position of the content of the node.
     */
    fun getTo() = getPropertyValue(AnalyzerCommons.Identifiers.HiddenTo) as? LxmReaderCursor

    /**
     * Sets the value of the to property.
     */
    fun setTo(cursor: IReaderCursor) {
        setProperty(AnalyzerCommons.Identifiers.To, LxmInteger.from(cursor.position()), isConstant = true,
                ignoreConstant = true)
        setProperty(AnalyzerCommons.Identifiers.HiddenTo, LxmReaderCursor(cursor), isConstant = true,
                ignoreConstant = true)
    }

    /**
     * Applies an offset to the current node and its children.
     */
    fun applyOffset(offset: IReaderCursor) {
        val newReader = offset.getReader()
        val offsetAsInt = offset.position()

        val nodes = LinkedList<LxmNode>()
        nodes.addLast(this)

        while (nodes.isNotEmpty()) {
            val node = nodes.removeFirst()
            val from = node.getFrom().primitive.position()
            val to = node.getTo()!!.primitive.position()

            node.setFrom(newReader.saveCursorAt(offsetAsInt + from)!!)
            node.setTo(newReader.saveCursorAt(offsetAsInt + to)!!)

            nodes.addAll(node.getChildrenAsList().map { it.dereference(bigNode, toWrite = true) as LxmNode })
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryClone(bigNode: BigNode) = LxmNode(bigNode, this)

    override fun getType(bigNode: BigNode): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(bigNode, toWrite = false)
        return context.getPropertyValue(NodeType.TypeName) as LxmReference
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
