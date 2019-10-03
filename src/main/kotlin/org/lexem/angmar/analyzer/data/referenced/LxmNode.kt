package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.io.*

/**
 * The lexem values of the Node type.
 */
internal class LxmNode : LxmObject {
    val name: String
    val from: ITextReaderCursor
    var to: ITextReaderCursor? = null

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(name: String, from: ITextReaderCursor) {
        this.name = name
        this.from = from
    }

    constructor(name: String, from: ITextReaderCursor, oldNode: LxmNode) : super(oldNode) {
        this.name = name
        this.from = from
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Adds the initial properties.
     */
    fun init(memory: LexemMemory): LxmNode {
        val list = LxmList()
        val reference = memory.add(list)
        setProperty(memory, AnalyzerCommons.Identifiers.Children, reference)

        return this
    }

    /**
     * Gets the children property value.
     */
    private fun getChildren(memory: LexemMemory) =
            getDereferencedProperty<LxmList>(memory, AnalyzerCommons.Identifiers.Children)!!

    /**
     * Gets the children property value as a list.
     */
    fun getChildrenAsList(memory: LexemMemory) = getChildren(memory).getAllCells()

    // OVERRIDE METHODS -------------------------------------------------------

    override fun clone() = if (isImmutable) {
        this
    } else {
        LxmNode(name, from, this)
    }

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, NodeType.TypeName)

    override fun toString() = "[NODE] $name @[from ${from.position()} to ${to?.position()}] = ${super.toString()}"
}
