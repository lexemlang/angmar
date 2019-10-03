package org.lexem.angmar.io.printer

import com.google.gson.*

/**
 * Interface for any element that could be represented as a tree.
 */
interface ITreeLikePrintable {
    /**
     * Generates the tree representation of an element.
     */
    fun toTree(): JsonObject {
        val result = JsonObject()
        result.addProperty("type", this.javaClass.simpleName)
        return result
    }
}
