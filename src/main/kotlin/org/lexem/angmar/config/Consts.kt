package org.lexem.angmar.config

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*

/**
 * Constants of the project.
 */
internal object Consts {
    // Flag to specify the system is under test.
    const val verbose = true

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
        const val spatialGarbageCollectorMinimumFreeSpace = 20.0
        const val spatialGarbageCollectorInitialThreshold = 40000
        const val spatialGarbageCollectorThresholdIncrement = 3.0
        const val heapPageBits = 8
        const val heapPageL1Mask = 0.inv() // 0xFFFFFFFF
        const val heapPageL2Mask = ((1 shl 8) - 1).inv() // 0xFFFFFF00
        const val heapPageL3Mask = ((1 shl 16) - 1).inv() // 0xFFFF0000
        const val heapPageL4Mask = ((1 shl 24) - 1).inv() // 0xFF000000
    }

    object Analyzer {
        const val defaultTimeoutInMilliseconds = 60000L // 60 seconds
        const val defaultTestTimeoutInMilliseconds = 300000L // 5 minutes
    }

    object Float {
        const val exponentialDefaultPrecision = 5
        const val maxStepsDuringToString = 7
    }

    object Node {
        val defaultPropertiesForExpression = mapOf(AnalyzerCommons.Properties.Capture to LxmLogic.True,
                AnalyzerCommons.Properties.Children to LxmLogic.True,
                AnalyzerCommons.Properties.Consume to LxmLogic.True,
                AnalyzerCommons.Properties.Property to LxmLogic.False,
                AnalyzerCommons.Properties.Insensible to LxmLogic.False,
                AnalyzerCommons.Properties.Backtrack to LxmLogic.False,
                AnalyzerCommons.Properties.Reverse to LxmLogic.False)

        val defaultPropertiesForExpressionGroup = mapOf(AnalyzerCommons.Properties.Capture to LxmLogic.False,
                AnalyzerCommons.Properties.Children to LxmLogic.True,
                AnalyzerCommons.Properties.Consume to LxmLogic.True,
                AnalyzerCommons.Properties.Property to LxmLogic.False,
                AnalyzerCommons.Properties.Insensible to LxmLogic.False,
                AnalyzerCommons.Properties.Backtrack to LxmLogic.True,
                AnalyzerCommons.Properties.Reverse to LxmLogic.False)

        val defaultPropertiesForFilter = mapOf(AnalyzerCommons.Properties.Capture to LxmLogic.True,
                AnalyzerCommons.Properties.Children to LxmLogic.True,
                AnalyzerCommons.Properties.Backtrack to LxmLogic.False,
                AnalyzerCommons.Properties.Reverse to LxmLogic.False)

        val defaultPropertiesForFilterGroup = mapOf(AnalyzerCommons.Properties.Capture to LxmLogic.False,
                AnalyzerCommons.Properties.Children to LxmLogic.True,
                AnalyzerCommons.Properties.Backtrack to LxmLogic.True,
                AnalyzerCommons.Properties.Consume to LxmLogic.True,
                AnalyzerCommons.Properties.Reverse to LxmLogic.False)
    }
}
