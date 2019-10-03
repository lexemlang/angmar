package org.lexem.angmar.io.printer

import com.google.gson.*

object TreeLikePrintable {
    /**
     * Maps a list of strings into a [JsonArray].
     */
    fun stringListToTest(list: Iterable<String>): JsonArray {
        val result = JsonArray()

        list.forEach { result.add(it) }

        return result
    }

    /**
     * Maps a list of [ITreeLikePrintable] into a [JsonArray].
     */
    fun listToTest(list: Iterable<ITreeLikePrintable>): JsonArray {
        val result = JsonArray()

        list.forEach { result.add(it.toTree()) }

        return result
    }
}
