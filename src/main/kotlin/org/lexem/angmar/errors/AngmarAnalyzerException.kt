package org.lexem.angmar.errors

import es.jtp.kterm.*
import org.lexem.angmar.config.*

/**
 * Generic exception to show an analyzer error and finish the execution.
 */
class AngmarAnalyzerException : AngmarLoggedException {
    val type: AngmarAnalyzerExceptionType

    constructor(type: AngmarAnalyzerExceptionType, logger: Logger) : super(logger) {
        this.type = type
    }

    constructor(type: AngmarAnalyzerExceptionType, message: String, builder: Logger.() -> Unit) : super(message,
            builder) {
        this.type = type
        this.logger.addNote(Consts.Logger.errorIdTitle, type.name)
    }

    override fun logMessage() {
        logger.logAsError()
    }
}
