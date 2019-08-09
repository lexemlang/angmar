package org.lexem.angmar.errors

import es.jtp.kterm.*
import org.lexem.angmar.config.*

/**
 * Generic exception to show a parsing error and finish the execution.
 */
class AngmarParserException : AngmarException {
    val type: AngmarParserExceptionType
    val logger: Logger

    constructor(type: AngmarParserExceptionType, logger: Logger) : super("") {
        this.type = type
        this.logger = logger
    }

    constructor(type: AngmarParserExceptionType, message: String, builder: LoggerBuilder.() -> Unit) : super("") {
        this.type = type
        this.logger = Logger.build(message) {
            builder(this)

            addNote(Consts.Logger.errorIdTitle, type.name)
        }
    }

    override fun logMessage() {
        logger.logAsError()
    }
}
