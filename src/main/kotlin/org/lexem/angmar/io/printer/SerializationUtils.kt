package org.lexem.angmar.io.printer

import com.google.gson.*

/**
 * Serialization utils for [JsonSerializable] objects.
 */
internal object SerializationUtils {
    /**
     * Maps a list of strings into a [JsonArray].
     */
    fun stringListToTest(list: Iterable<String>): JsonArray {
        val result = JsonArray()

        list.forEach { result.add(it) }

        return result
    }

    /**
     * Maps a list of [JsonSerializable] into a [JsonArray].
     */
    fun listToTest(list: Iterable<JsonSerializable>): JsonArray {
        val result = JsonArray()

        list.forEach { result.add(it.toTree()) }

        return result
    }

    /**
     * Maps a list of [JsonSerializable] into a [JsonArray].
     */
    fun nullableListToTest(list: Iterable<JsonSerializable?>): JsonArray {
        val result = JsonArray()

        list.forEach { result.add(it?.toTree()) }

        return result
    }
}
