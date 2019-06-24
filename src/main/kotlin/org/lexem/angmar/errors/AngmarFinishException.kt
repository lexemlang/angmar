package org.lexem.angmar.errors

import es.jtp.kterm.*

/**
 * Generic exception to finish send a finish message.
 */
open class AngmarFinishException(val logger: Logger? = null) : AngmarException("") {
    override fun logMessage() {
        logger?.let {
            logger.logAsError()
        }
    }
}