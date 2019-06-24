package org.lexem.angmar.io.printer

import es.jtp.kterm.utils.*

/**
 * Json printer for any tree-like data.
 */
class JsonPrinter private constructor() : TreeLikePrinter() {
    override fun <T> addField(name: String, value: T?) {
        if (value == null) {
            sb.append("$currentIndent\"$name\": $value,\n")
        }

        when (value) {
            is Char -> sb.append("$currentIndent\"$name\": \"${value.toString().stringify()}\",\n")
            is String -> sb.append("$currentIndent\"$name\": \"${value.stringify()}\",\n")
            is ITreeLikePrintable -> {
                sb.append("$currentIndent\"$name\": ")
                printObject(value)
                sb.append(",\n")
            }
            else -> sb.append("$currentIndent\"$name\": $value,\n")
        }
    }

    override fun <T> addField(name: String, values: Iterable<T>?) {
        if (values == null) {
            sb.append("$currentIndent\"$name\": $values,\n")
        }

        val el = values?.firstOrNull()
        if (el == null) {
            sb.append("$currentIndent\"$name\": [],\n")
            return
        }

        sb.append("$currentIndent\"$name\": ")
        @Suppress("UNCHECKED_CAST") when (el) {
            is Char, is String -> printStringList(values)
            is ITreeLikePrintable -> printObjectList(values as Iterable<ITreeLikePrintable>)
            else -> printList(values)
        }
        sb.append(",\n")
    }

    private fun printObject(element: ITreeLikePrintable) {
        sb.append("{\n")
        val savedIndent = currentIndent
        currentIndent += TreeIndentStep

        addField("\$type", element.getTypeName())
        element.toTree(this)
        sb.deleteCharAt(sb.lastIndex - 1)

        currentIndent = savedIndent
        sb.append("$currentIndent}")
    }

    private fun <T> printList(elements: Iterable<T>) {
        sb.append("[\n")
        val savedIndent = currentIndent
        currentIndent += TreeIndentStep

        for (element in elements) {
            sb.append("$currentIndent\"${element.toString()}\",\n")
        }
        sb.deleteCharAt(sb.lastIndex - 1)

        currentIndent = savedIndent
        sb.append("$currentIndent]")
    }

    private fun <T> printStringList(elements: Iterable<T>) {
        sb.append("[\n")
        val savedIndent = currentIndent
        currentIndent += TreeIndentStep

        for (element in elements) {
            sb.append("$currentIndent\"${element.toString().stringify()}\",\n")
        }
        sb.deleteCharAt(sb.lastIndex - 1)

        currentIndent = savedIndent
        sb.append("$currentIndent]")
    }

    private fun printObjectList(elements: Iterable<ITreeLikePrintable>) {
        sb.append("[\n")
        val savedIndent = currentIndent
        currentIndent += TreeIndentStep

        for (element in elements) {
            sb.append(currentIndent)
            printObject(element)
            sb.append(",\n")
        }
        sb.deleteCharAt(sb.lastIndex - 1)

        currentIndent = savedIndent
        sb.append("$currentIndent]")
    }

    companion object {
        private const val TreeIndentStep = "   "

        /**
         * Prints an element into a tree representation.
         */
        fun print(element: ITreeLikePrintable): String {
            val res = JsonPrinter()
            res.printObject(element)
            return res.toString()
        }
    }
}