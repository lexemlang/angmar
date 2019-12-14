package org.lexem.angmar.config

/**
 * Constants of the project.
 */
internal object Consts {
    // Flag to specify the system is under test.
    const val debug = false

    const val projectVersion = "0.1.1"
    const val defaultEntryPoint = "main"

    object Commands {
        const val parser = "angmar"
    }

    object Logger {
        const val codeTitle = "Code"
        const val hintTitle = "Hint"
        const val errorIdTitle = "ERID"
    }

    object Memory {
        const val minimumFreeSpace = 20.0
        const val garbageThresholdIncrement = 2.0
        const val maxPoolSize = 50
    }

    object Analyzer {
        const val defaultTimeoutInMilliseconds = 60000L // 60 seconds
        const val defaultTestTimeoutInMilliseconds = 300000L // 5 minutes
    }

    object Float {
        const val exponentialDefaultPrecision = 5
        const val maxStepsDuringToString = 7
    }
}
