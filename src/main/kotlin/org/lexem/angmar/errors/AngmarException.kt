package org.lexem.angmar.errors

import es.jtp.kterm.*

/**
 * Generic exception for the project.
 */
open class AngmarException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Throwable) : super(message, exception)

    /**
     * Logs this exception to the terminal.
     */
    open fun logMessage() {
        Logger.error(this) {
            showDate = true
            showThread = true
            showStackNumbers = true
        }
    }
}
