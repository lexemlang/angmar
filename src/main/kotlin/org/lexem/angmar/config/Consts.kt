package org.lexem.angmar.config

/**
 * Constants of the project.
 */
internal object Consts {
    // Flag to specify the system is under test.
    const val debug = true

    const val projectVersion = "0.1.1"
    const val defaultEntryPoint = "main"

    object Commands {
        const val parser = "angmar"
    }

    object Logger {
        const val codeTitle = "Code"
        const val hintTitle = "Hint"
        const val tagTitle = "Tag"
        const val errorIdTitle = "ERRID"
    }

    object Memory {
        const val minimumFreeSpace = 20.0
        const val maxPoolSize = 50
        const val maxDistanceToShift = 10
        const val maxVersionCountToFullyCopyAValue = 20
        const val spatialGarbageCollectorInitialThreshold = 1000
        const val spatialGarbageCollectorThresholdIncrement = 2.0
        const val temporalGarbageCollectorThreshold = 15000
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
