package org.lexem.angmar.io.printer

/**
 * Interface for any element that could be represented as a tree.
 */
interface ITreeLikePrintable {
    /**
     * Gets the type of the current element.
     */
    fun getTypeName() = this.javaClass.simpleName

    /**
     * Generates the tree representation of an element.
     */
    fun toTree(printer: TreeLikePrinter)
}