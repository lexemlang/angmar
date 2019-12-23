package org.lexem.angmar.errors

import es.jtp.kterm.*
import org.lexem.angmar.config.*

/**
 * Generic exception to show a compiler error and finish the execution.
 */
class AngmarCompilerException : AngmarLoggedException {
    val type: AngmarCompilerExceptionType

    constructor(type: AngmarCompilerExceptionType, message: String, builder: Logger.() -> Unit) : super(message,
            builder) {
        this.type = type
        this.logger.addNote(Consts.Logger.errorIdTitle, type.name)
    }

    override fun logMessage() {
        logger.logAsError()
    }
}
