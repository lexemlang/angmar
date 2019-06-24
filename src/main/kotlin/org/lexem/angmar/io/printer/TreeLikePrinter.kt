package org.lexem.angmar.io.printer

/**
 * Printer pattern for any tree-like data.
 */
abstract class TreeLikePrinter {
    protected val sb: StringBuilder = StringBuilder()
    protected var currentIndent = ""

    /**
     * Adds a field.
     */
    abstract fun <T> addField(name: String, value: T?)

    /**
     * Adds a list field.
     */
    abstract fun <T> addField(name: String, values: Iterable<T>?)

    /**
     * Adds an optional field.
     */
    open fun <T> addOptionalField(name: String, value: T?) {
        addField(name, value ?: return)
    }

    /**
     * Adds an optional list field.
     */
    open fun <T> addOptionalField(name: String, values: Iterable<T>?) {
        addField(name, values ?: return)
    }

    override fun toString() = sb.toString()
}