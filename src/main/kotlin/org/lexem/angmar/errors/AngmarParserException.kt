package org.lexem.angmar.errors

import es.jtp.kterm.*

/**
 * Generic exception to show a parsing error and finish the execution.
 */
class AngmarParserException(val logger: Logger) : AngmarException("") {
    override fun logMessage() {
        logger.logAsError()
    }
}