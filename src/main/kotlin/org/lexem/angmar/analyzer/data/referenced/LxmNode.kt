package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*

/**
 * The Lexem values of the Node type.
 */
internal class LxmNode : LxmObject {
    val name: String

    // CONSTRUCTORS -----------------------------------------------------------

    private constructor(memory: LexemMemory, oldVersion: LxmNode, toClone: Boolean) : super(memory, oldVersion,
            toClone) {
        this.name = oldVersion.name
    }

    constructor(name: String, from: IReaderCursor, parent: LxmNode?, memory: LexemMemory) : super(memory) {
        this.name = name

        if (parent != null) {
            setProperty(memory, AnalyzerCommons.Identifiers.Parent, parent.getPrimitive(), isConstant = true)
        }

        setProperty(memory, AnalyzerCommons.Identifiers.Name, LxmString.from(name), isConstant = true)
        setFrom(memory, from)

        init(memory)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Adds the initial properties.
     */
    private fun init(memory: LexemMemory) {
        val children = LxmList(memory)
        children.makeConstant(memory)
        setProperty(memory, AnalyzerCommons.Identifiers.Children, children, isConstant = true)

        val properties = LxmObject(memory)
        setProperty(memory, AnalyzerCommons.Identifiers.Properties, properties, isConstant = true)
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
    fun setParent(memory: LexemMemory, parent: LxmNode) =
            setProperty(memory, AnalyzerCommons.Identifiers.Parent, parent, isConstant = true, ignoringConstant = true)

    /**
     * Gets the children.
     */
    fun getChildren(memory: LexemMemory, toWrite: Boolean) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Children, toWrite)!!

    /**
     * Gets the children property value as a list.
     * TODO convert this to sequence.
     */
    fun getChildrenAsList(memory: LexemMemory) = getChildren(memory, toWrite = false).getAllCells()

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
    fun setFrom(memory: LexemMemory, cursor: IReaderCursor) {
        setProperty(memory, AnalyzerCommons.Identifiers.From, LxmInteger.from(cursor.position()), isConstant = true,
                ignoringConstant = true)
        setProperty(memory, AnalyzerCommons.Identifiers.HiddenFrom, LxmReaderCursor(cursor), isConstant = true,
                ignoringConstant = true)
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
                ignoringConstant = true)
        setProperty(memory, AnalyzerCommons.Identifiers.HiddenTo, LxmReaderCursor(cursor), isConstant = true,
                ignoringConstant = true)
    }

    /**
     * Applies the default properties for expressions.
     */
    fun applyDefaultPropertiesForExpression(memory: LexemMemory) {
        val props = getProperties(memory, toWrite = true)
        props.setProperty(memory, AnalyzerCommons.Properties.Capture, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Children, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Consume, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Property, LxmLogic.False)
        props.setProperty(memory, AnalyzerCommons.Properties.Insensible, LxmLogic.False)
        props.setProperty(memory, AnalyzerCommons.Properties.Backtrack, LxmLogic.False)
        props.setProperty(memory, AnalyzerCommons.Properties.Reverse, LxmLogic.False)
    }

    /**
     * Applies the default properties for filters.
     */
    fun applyDefaultPropertiesForFilter(memory: LexemMemory) {
        val props = getProperties(memory, toWrite = true)
        props.setProperty(memory, AnalyzerCommons.Properties.Capture, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Children, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Backtrack, LxmLogic.False)
        props.setProperty(memory, AnalyzerCommons.Properties.Reverse, LxmLogic.False)
    }

    /**
     * Applies the default properties for groups.
     */
    fun applyDefaultPropertiesForGroup(memory: LexemMemory) {
        val props = getProperties(memory, toWrite = true)
        props.setProperty(memory, AnalyzerCommons.Properties.Children, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Backtrack, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Consume, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Capture, LxmLogic.False)
        props.setProperty(memory, AnalyzerCommons.Properties.Property, LxmLogic.False)
        props.setProperty(memory, AnalyzerCommons.Properties.Insensible, LxmLogic.False)
        props.setProperty(memory, AnalyzerCommons.Properties.Reverse, LxmLogic.False)
    }

    /**
     * Applies the default properties for filter groups.
     */
    fun applyDefaultPropertiesForFilterGroup(memory: LexemMemory) {
        val props = getProperties(memory, toWrite = true)
        props.setProperty(memory, AnalyzerCommons.Properties.Children, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Backtrack, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Consume, LxmLogic.True)
        props.setProperty(memory, AnalyzerCommons.Properties.Capture, LxmLogic.False)
        props.setProperty(memory, AnalyzerCommons.Properties.Reverse, LxmLogic.False)
    }

    /**
     * Applies an offset to the current node and its children.
     */
    fun applyOffset(memory: LexemMemory, offset: IReaderCursor) {
        val newReader = offset.getReader()
        val currentCursor = newReader.saveCursor()
        var prevCursor = getFrom(memory).primitive

        // Update from.
        setFrom(memory, offset)
        offset.restore()

        // Update children.
        for (child in getChildrenAsList(memory)) {
            val childNode = child.dereference(memory, toWrite = true) as LxmNode

            // Calculate and set the offset.
            val difference = childNode.getFrom(memory).primitive.position() - prevCursor.position()
            newReader.advance(difference)

            // Save the end of the current child.
            prevCursor = childNode.getTo(memory)!!.primitive

            childNode.applyOffsetWithoutRestoring(memory, newReader)
        }

        // Update to.
        val difference = getTo(memory)!!.primitive.position() - prevCursor.position()
        newReader.advance(difference)
        setTo(memory, newReader.saveCursor())

        // Restore the position of the reader.
        currentCursor.restore()
    }

    private fun applyOffsetWithoutRestoring(memory: LexemMemory, reader: IReader) {
        var prevCursor = getFrom(memory).primitive

        // Update from.
        setFrom(memory, reader.saveCursor())

        // Update children.
        for (child in getChildrenAsList(memory)) {
            val childNode = child.dereference(memory, toWrite = true) as LxmNode

            // Calculate and set the offset.
            val difference = childNode.getFrom(memory).primitive.position() - prevCursor.position()
            reader.advance(difference)

            // Save the end of the current child.
            prevCursor = childNode.getTo(memory)!!.primitive

            childNode.applyOffsetWithoutRestoring(memory, reader)
        }

        // Update to.
        val difference = getTo(memory)!!.primitive.position() - prevCursor.position()
        reader.advance(difference)
        setTo(memory, reader.saveCursor())
    }

    /**
     * De-allocates the current branch, removing the node from its parent.
     */
    fun memoryDeallocBranch(memory: LexemMemory) {
        // Remove children.
        for (child in getChildrenAsList(memory).map { it.dereference(memory, toWrite = true) as LxmNode }) {
            child.memoryDeallocBranchRecursively(memory)
        }

        // Remove from parent.
        val parent = getParent(memory, toWrite = false)
        if (parent != null) {
            val index = parent.getChildrenAsList(memory).map { it.dereference(memory, toWrite = false) as LxmNode }
                    .indexOf(this)
            if (index < 0) {
                throw AngmarUnreachableException()
            }

            parent.getChildren(memory, toWrite = true).removeCell(memory, index, ignoreConstant = true)
        }

        // Dealloc the current one.
        memoryDealloc(memory)
    }

    private fun memoryDeallocBranchRecursively(memory: LexemMemory) {
        // Remove children.
        for (child in getChildrenAsList(memory).map { it.dereference(memory, toWrite = true) as LxmNode }) {
            child.memoryDeallocBranchRecursively(memory)
        }

        // Dealloc the current one.
        memoryDealloc(memory)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun memoryShift(memory: LexemMemory) =
            LxmNode(memory, this, toClone = countOldVersions() >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, NodeType.TypeName) as LxmReference
    }

    override fun toString() = "[Node] $name = ${super.toString()}"
}
