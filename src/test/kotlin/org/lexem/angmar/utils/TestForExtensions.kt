package org.lexem.angmar.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.junit.jupiter.api.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.system.*

internal class TestForExtensions {
    @Test
    fun `test coroutines own sync`() = runBlocking {
        val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

        val maximum = 10000000L
        val synchronizer = AtomicBoolean(false)
        var count1 = 0L
        var count2 = 0L
        var flag = false

        val time = measureTimeMillis {
            GlobalScope.launch(dispatcher) {
                launch {
                    for (i in 0 until maximum) {
                        synchronizer.synchronize {
                            if (flag) {
                                count1 += 1
                            } else {
                                count2 += 1
                            }
                        }

                        for (j in 0 until 1000) {
                        }
                    }
                }

                launch {
                    synchronizer.synchronize {
                        if (count1 != maximum) {
                            flag = true
                        }
                    }
                }
            }.join()
        }

        println("With own sync: $time")

        dispatcher.close()

        Assertions.assertTrue(count1 != 0L, "count1 must have changed: $count1 . $count2")
        Assertions.assertTrue(count2 != 0L, "count2 must have changed: $count1 - $count2")
        Assertions.assertEquals(maximum, count1 + count2, "the sum is incorrect: ${count1 + count2}")
        Assertions.assertTrue(flag, "the flag must be true")
    }

    @Test
    fun `test coroutines with sync`() = runBlocking {
        val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
        val maximum = 10000000L
        val synchronizer = AtomicBoolean(false)
        var flag = false

        val obj = object {
            var count1 = 0L
            var count2 = 0L

            @Synchronized
            fun readCount1() = count1

            @Synchronized
            fun incCount1() {
                count1 += 1
            }

            @Synchronized
            fun incCount2() {
                count2 += 1
            }
        }

        val time = measureTimeMillis {
            GlobalScope.launch(dispatcher) {
                launch {
                    for (i in 0 until maximum) {
                        synchronizer.synchronize {
                            if (flag) {
                                obj.incCount1()
                            } else {
                                obj.incCount2()
                            }
                        }

                        for (j in 0 until 1000) {
                        }
                    }
                }

                launch {
                    synchronizer.synchronize {
                        if (obj.readCount1() != maximum) {
                            flag = true
                        }
                    }
                }
            }.join()
        }

        println("With sync: $time")

        dispatcher.close()

        Assertions.assertTrue(obj.count1 != 0L, "count1 must have changed: ${obj.count1} - ${obj.count2}")
        Assertions.assertTrue(obj.count2 != 0L, "count2 must have changed: ${obj.count1} - ${obj.count2}")
        Assertions.assertEquals(maximum, obj.count1 + obj.count2, "the sum is incorrect: ${obj.count1 + obj.count2}")
        Assertions.assertTrue(flag, "the flag must be true")
    }

    @Test
    fun `remove`() = runBlocking {
        val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
        val channel = Channel<Int>(10)

        GlobalScope.launch(dispatcher) {
            launch {
                try {
                    while (true) {
                        // Wait for value.
                        var value: Int? = channel.receive()

                        // Get all available.
                        val values = mutableListOf<Int>()
                        while (value != null) {
                            values.add(value)

                            value = channel.poll()
                        }

                        println("Received: $values")

                        delay(3000)
                    }
                } catch (e: ClosedReceiveChannelException) {
                    println("End of reception")
                }
            }

            launch {
                for (i in 0 until 10) {
                    println("Send: $i")
                    channel.send(i)

                    delay(500)
                }

                println("End of send")
                channel.close()
            }
        }.join()

        println("End of all")
    }
}
