package org.lexem.angmar

import com.uchuhimo.konf.*
import es.jtp.kterm.*
import org.lexem.angmar.commands.*
import org.lexem.angmar.errors.*

internal fun main(args: Array<String>) {
    try {
        AngmarCommand().main(args)
    } catch (e: AngmarLoggedException) {
        e.logMessage()
    } catch (e: UnsetValueException) {
        Logger.error("The configuration file is incorrect.\nThe required property '${e.name.replace("item ",
                "")}' is missing.")
    } catch (e: Throwable) {
        Logger.error(e) {
            showDate = true
            showThread = true
            showStackNumbers = true
        }
    }
}
