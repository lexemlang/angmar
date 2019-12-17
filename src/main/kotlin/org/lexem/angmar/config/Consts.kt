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
        const val debugLogEachMilliseconds = 20000L // 20 seconds
    }

    object Logger {
        const val codeTitle = "Code"
        const val hintTitle = "Hint"
        const val tagTitle = "Tag"
        const val errorIdTitle = "ERRID"
    }

    object Memory {
        const val maxPoolSize = 500
        const val maxDistanceToShift = 5
        const val maxVersionCountToFullyCopyAValue = 15
        const val spatialGarbageCollectorMinimumFreeSpace = 20.0
        const val spatialGarbageCollectorInitialThreshold = 10000
        const val spatialGarbageCollectorThresholdIncrement = 3.0
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
