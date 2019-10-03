package org.lexem.angmar.utils

import kotlin.system.*

/**
 * Utilities about time.
 */
object TimeUtils {

    /**
     *  Executes the given block and returns elapsed time in seconds.
     */
    fun measureTimeSeconds(block: () -> Unit): Double {
        val time = measureTimeMillis(block)
        return time.toDouble() / 1000.0
    }
}
