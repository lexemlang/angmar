package org.lexem.angmar.data

import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import java.util.concurrent.*

internal class SerialSynchronizerTest {
    @RepeatedTest(5)
    fun `test sync`() {
        val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
        var count = 0L
        val maximum = 1000000L
        val coroutineCount = 10
        val synchronizer = SerialSynchronizer()

        runBlocking {
            GlobalScope.launch(dispatcher) {
                for (i in 0 until coroutineCount) {
                    launch {
                        delay(1)
                        for (j in 0 until maximum) {
                            synchronizer.sync {
                                count += 1
                            }
                        }
                    }
                }
            }.join()
        }

        Assertions.assertEquals(maximum * coroutineCount, count, "The count is incorrect")
    }
}
