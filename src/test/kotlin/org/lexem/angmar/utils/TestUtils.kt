package org.lexem.angmar.utils

import org.lexem.angmar.errors.*

/**
 * Ensures an [AngmarParserException] or throws an error.
 */
internal inline fun assertParserException(fn: () -> Unit) {
    try {
        fn()
        throw Exception("This method should throw an AngmarParserException")
    } catch (e: AngmarParserException) {
        e.logMessage()
    }
}
