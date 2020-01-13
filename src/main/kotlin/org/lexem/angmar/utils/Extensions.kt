package org.lexem.angmar.utils

import com.github.ajalt.clikt.core.*
import java.io.*

/**
 * Gets the parent of this file if it has any or the root otherwise.
 */
fun File.parentFileOrRoot() = this.parentFile ?: File("/")


/**
 * Gets the parent of this file if it has any or the relative root otherwise.
 */
fun File.parentFileOrRelativeRoot() = this.parentFile ?: File("./")

/**
 * Gets the parent command as the specified type.
 */
internal inline fun <reified T : CliktCommand> CliktCommand.parentAs() = this.context.parent!!.command as T

/**
 * Returns one of both values depending on the condition is null.
 */
fun <T, R> T?.orDefault(default: R, thenValue: (T) -> R): R = if (this != null) {
    thenValue(this)
} else {
    default
}

/**
 * Transforms a [String] to lowercase following the Unicode rules.
 */
fun String.toUnicodeLowercase() = (0 until Character.codePointCount(this, 0, this.length)).map {
    var point = Character.codePointAt(this, it)
    point = Character.toLowerCase(point)
    Character.toChars(point).joinToString("")
}.joinToString("")

/**
 * Transforms a [Char] to lowercase following the Unicode rules.
 */
fun Char.toUnicodeLowercase() = Character.toLowerCase(this)

/**
 * Reverses the bits of the byte.
 */
fun Byte.reverseBits(): Byte {
    var itInt = toInt() and 0xFF
    var res = 0
    while (itInt != 0) {
        res = res shl 1
        res = res or (itInt and 1)
        itInt = itInt shr 1
    }

    return res.toByte()
}

/**
 * Transforms a [String] to lowercase following the Unicode rules.
 */
fun String.toUnicodeUppercase() = (0 until Character.codePointCount(this, 0, this.length)).map {
    var point = Character.codePointAt(this, it)
    point = Character.toUpperCase(point)
    Character.toChars(point).joinToString("")
}.joinToString("")

/**
 * Transforms a [Char] to lowercase following the Unicode rules.
 */
fun Char.toUnicodeUppercase() = Character.toUpperCase(this)

/**
 * Returns a new mutable map containing all key-value pairs from the original map.
 */
fun <K, V> Map<out K, V>.toHashMap(): HashMap<K, V> = HashMap(this)
