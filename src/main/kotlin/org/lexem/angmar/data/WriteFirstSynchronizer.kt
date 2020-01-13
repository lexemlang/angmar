package org.lexem.angmar.data

import java.util.concurrent.atomic.*

/**
 * A light lock to synchronize read ad write operations in multi-thread mode.
 * The writing operations has preference over reading ones.
 */
internal class WriteFirstSynchronizer {
    private val readerCount = AtomicInteger(0)
    private val synchronizer = SerialSynchronizer()

    // METHODS ----------------------------------------------------------------

    /**
     * Executes the function atomically in reading mode.
     */
    fun syncToRead(fn: () -> Unit) = syncLetToRead(fn)

    /**
     * Executes the function atomically in reading mode returning a value.
     */
    fun <T> syncLetToRead(fn: () -> T): T {
        synchronizer.sync {
            readerCount.incrementAndGet()
        }

        // Execute the function.
        try {
            val result = fn()

            // Free the use.
            readerCount.decrementAndGet()

            return result
        } catch (e: Throwable) {
            // Free the use.
            readerCount.decrementAndGet()

            throw e
        }
    }

    /**
     * Executes the function atomically in writing mode.
     */
    fun syncToWrite(fn: () -> Unit) = syncLetToWrite(fn)

    /**
     * Executes the function atomically in reading mode returning a value.
     */
    fun <T> syncLetToWrite(fn: () -> T): T {
        return synchronizer.syncLet {
            // Wait until no readers.
            while (readerCount.get() != 0) {
            }

            // Execute the function.
            fn()
        }
    }
}
