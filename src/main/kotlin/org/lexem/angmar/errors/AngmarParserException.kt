package org.lexem.angmar.errors

import es.jtp.kterm.*
import org.lexem.angmar.config.*

/**
 * Generic exception to show a parsing error and finish the execution.
 */
class AngmarParserException : AngmarLoggedException {
    val type: AngmarParserExceptionType

    constructor(type: AngmarParserExceptionType, message: String, builder: Logger.() -> Unit) : super(message,
            builder) {
        this.type = type
        this.logger.addNote(Consts.Logger.errorIdTitle, type.name)
    }

    override fun logMessage() {
        logger.logAsError()
    }
}
