package org.lexem.angmar.errors

import es.jtp.kterm.*

/**
 * Generic exception with a logger.
 */
open class AngmarLoggedException : AngmarException {
    val logger: Logger

    constructor(logger: Logger) : super(logger.message) {
        this.logger = logger
    }

    constructor(message: String, builder: Logger.() -> Unit) : super(message) {
        this.logger = Logger(message) {
            builder(this)
        }
    }

    override fun logMessage() {
        logger.logAsError()
    }
}
