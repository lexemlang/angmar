package org.lexem.angmar.data

import java.util.concurrent.atomic.*

/**
 * A light lock to synchronize operations in multi-thread mode.
 */
internal class SerialSynchronizer {
    private val synchronizer = AtomicBoolean(false)

    // METHODS ----------------------------------------------------------------

    /**
     * Executes the function atomically.
     */
    fun sync(fn: () -> Unit) = syncLet(fn)

    /**
     * Executes the function atomically returning a value.
     */
    fun <T> syncLet(fn: () -> T): T {
        // Wait until get the use.
        var isBeingUsed = synchronizer.getAndSet(true)
        while (isBeingUsed) {
            isBeingUsed = synchronizer.getAndSet(true)
        }

        // Execute the function.
        try {
            val result = fn()

            // Free the use.
            synchronizer.set(false)

            return result
        } catch (e: Throwable) {
            // Free the use.
            synchronizer.set(false)

            throw e
        }
    }
}
