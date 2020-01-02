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

    constructor(memory: IMemory, name: String, from: IReaderCursor, type: LxmNodeType = LxmNodeType.Custom) : super(
            memory) {
        this.name = name
        this.type = type

        setProperty(memory, AnalyzerCommons.Identifiers.Name, LxmString.from(name), isConstant = true)
        setFrom(memory, from)
        setParentIndex(memory, -1)

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
        val children = LxmList(memory)
        children.makeConstant(memory)
        setProperty(memory, AnalyzerCommons.Identifiers.Children, children, isConstant = true)

        val properties = LxmObject(memory)
        setProperty(memory, AnalyzerCommons.Identifiers.Properties, properties, isConstant = true)

        val defaultProperties = AnalyzerCommons.getDefaultPropertiesByType(type)
        for ((key, value) in defaultProperties) {
            properties.setProperty(memory, key, value)
        }
    }

    /**
     * Adds the node to the parent.
     */
    fun addToParent(memory: IMemory, parent: LxmNode) {
        // Remove from previously parent.
        removeFromParent(memory)

        val parentList = parent.getChildren(memory, toWrite = true)

        setParentIndex(memory, parentList.size)
        setProperty(memory, AnalyzerCommons.Identifiers.Parent, parent, isConstant = true, ignoreConstant = true)
        parentList.addCell(memory, this, ignoreConstant = true)
    }

    /**
     * Adds the node to the parent.
     */
    fun removeFromParent(memory: IMemory) {
        val parent = getParent(memory, toWrite = false) ?: return
        val parentList = parent.getChildren(memory, toWrite = true)
        val parentIndex = getParentIndex(memory)

        for (i in parentIndex + 1 until parentList.size) {
            val node = parentList.getDereferencedCell<LxmNode>(memory, i, toWrite = true)!!
            node.setParentIndex(memory, node.getParentIndex(memory) - 1)
        }

        parentList.removeCell(memory, parentIndex, ignoreConstant = true)

        setParentIndex(memory, -1)
        removeProperty(memory, AnalyzerCommons.Identifiers.Parent, ignoreConstant = true)
    }

    /**
     * Adds a list of nodes at the specified position.
     */
    fun insertChildren(memory: IMemory, children: List<LxmNode>, at: Int) {
        // Remove from previously parent and set new parent.
        for (node in children) {
            node.removeFromParent(memory)

            node.setParent(memory, this)
        }

        val childList = getChildren(memory, toWrite = true)
        childList.insertCell(memory, at, *children.toTypedArray(), ignoreConstant = true)

        for (i in at until childList.size) {
            val node = childList.getDereferencedCell<LxmNode>(memory, i, toWrite = true)!!
            node.setParentIndex(memory, i)
        }
    }

    /**
     * Moves all the children of this node to its parent.
     */
    fun replaceNodeInParentByChildren(memory: IMemory) {
        val parent = getParent(memory, toWrite = false) ?: return
        val parentList = parent.getChildren(memory, toWrite = true)
        val childList = getChildrenAsList(memory).map { it.dereference(memory, toWrite = false) as LxmNode }
        val parentIndex = getParentIndex(memory)

        // Set the new parent and indexes.
        for ((i, node) in childList.withIndex()) {
            node.setParent(memory, parent)
            node.setParentIndex(memory, parentIndex + i)
        }

        for (index in parentIndex + 1 until parentList.size) {
            val node = parentList.getCell(index)!!.dereference(memory, toWrite = true) as LxmNode
            node.setParentIndex(memory, index)
        }

        parentList.replaceCell(memory, parentIndex, 1, *childList.toList().toTypedArray(), ignoreConstant = true)
    }

    /**
     * Clears the children of the node.
     */
    fun clearChildren(memory: IMemory) {
        val childList = getChildren(memory, toWrite = true)

        for (i in 0 until childList.size) {
            val node = childList.getDereferencedCell<LxmNode>(memory, i, toWrite = true)!!
            node.setParentIndex(memory, -1)
            node.removeProperty(memory, AnalyzerCommons.Identifiers.Parent, ignoreConstant = true)
        }

        childList.removeCell(memory, 0, childList.size, ignoreConstant = true)
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
    fun setParent(memory: IMemory, parent: LxmNode) =
            setProperty(memory, AnalyzerCommons.Identifiers.Parent, parent, isConstant = true, ignoreConstant = true)

    /**
     * Gets the index in the parent node.
     */
    fun getParentIndex(memory: IMemory) =
            (getPropertyValue(memory, AnalyzerCommons.Identifiers.ParentIndex) as LxmInteger).primitive

    /**
     * Sets the index in the parent node.
     */
    fun setParentIndex(memory: IMemory, index: Int) =
            setProperty(memory, AnalyzerCommons.Identifiers.ParentIndex, LxmInteger.from(index), isConstant = true,
                    ignoreConstant = true)

    /**
     * Gets the children.
     */
    fun getChildren(memory: IMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Children, toWrite)!!

    /**
     * Gets the children property value as a list.
     */
    fun getChildrenAsList(memory: IMemory) = getChildren(memory, toWrite = false).getAllCells()

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
    fun setFrom(memory: IMemory, cursor: IReaderCursor) {
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
     * Applies an offset to the current node and its children.
     */
    fun applyOffset(memory: IMemory, offset: IReaderCursor) {
        val newReader = offset.getReader()
        val offsetAsInt = offset.position()

        val nodes = LinkedList<LxmNode>()
        nodes.addLast(this)

        while (nodes.isNotEmpty()) {
            val node = nodes.removeFirst()
            val from = node.getFrom(memory).primitive.position()
            val to = node.getTo(memory)!!.primitive.position()

            node.setFrom(memory, newReader.saveCursorAt(offsetAsInt + from)!!)
            node.setTo(memory, newReader.saveCursorAt(offsetAsInt + to)!!)

            nodes.addAll(node.getChildrenAsList(memory).map { it.dereference(memory, toWrite = true) as LxmNode })
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
