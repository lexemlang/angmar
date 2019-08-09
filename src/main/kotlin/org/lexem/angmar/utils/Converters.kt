package org.lexem.angmar.utils

/**
 * Gathers all converters of Angmar.
 */
object Converters {
    /**
     * Converts the hexadecimal input to a Double.
     */
    fun hexToDouble(hex: String) = if (hex.length <= 7) {
        hex.toUpperCase().toLong(16).toDouble()
    } else {
        hex.toUpperCase().toBigInteger(16).toDouble()
    }
}