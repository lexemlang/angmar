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
fun String.toUnicodeLowercase() = this.map { it.toUnicodeLowercase() }.joinToString("")

/**
 * Transforms a [Char] to lowercase following the Unicode rules.
 */
fun Char.toUnicodeLowercase(): Char {
    // TODO generalize toLowerCase with unicode
    return this.toLowerCase()
}

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
fun String.toUnicodeUppercase() = this.map { it.toUnicodeUppercase() }.joinToString("")

/**
 * Transforms a [Char] to lowercase following the Unicode rules.
 */
fun Char.toUnicodeUppercase(): Char {
    // TODO generalize toLowerCase with unicode
    return this.toLowerCase()
}
