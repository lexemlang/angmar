package org.lexem.angmar.analyzer.memory

internal class LexemMemoryLoadTest {
    // To use this tests make BigNode.heap internal.
    val repetitions = 10
    val heapSizeSameRangeTest = 200 // Must be a multiple of 100
    val heapSizePyramidTest = 15000 // Must be a multiple of 100

    //    @Test
    //    @Nightly
    //    fun `load test - collapseTo - putAll method - same range case`() {
    //        repeatToAverage {
    //            sameRangeExecutor { memory, destination ->
    //                var node: BigNode? = destination.nextNode
    //                while (node != null) {
    //                    destination.heap.putAll(node.heap)
    //
    //                    node = node.nextNode
    //                }
    //
    //                for (i in destination.heap.map { it.value.value }) {
    //                    i?.bigNode = destination
    //                }
    //            }
    //        }
    //    }
    //
    //    @Test
    //    @Nightly
    //    fun `load test - collapseTo - loop method - same range case`() {
    //        repeatToAverage {
    //            sameRangeExecutor { memory, destination ->
    //                // Unlink
    //                destination.nextNode!!.previousNode = null
    //
    //                var node: BigNode? = memory.lastNode
    //                while (node != null) {
    //                    for ((i, cell) in node.heap) {
    //                        // Remove the node from previous.
    //                        var node2: BigNode? = node.previousNode
    //                        while (node2 != null) {
    //                            node2.heap.remove(i)
    //
    //                            node2 = node2.previousNode
    //                        }
    //
    //                        // Set in destination.
    //                        destination.heap[i] = cell
    //                        cell.value?.bigNode = destination
    //                    }
    //
    //                    node = node.previousNode
    //                }
    //            }
    //        }
    //    }
    //
    //    @Test
    //    @Nightly
    //    fun `load test - collapseTo - putAll method - pyramid range case`() {
    //        repeatToAverage {
    //            pyramidRangeExecutor { memory, destination ->
    //                var node: BigNode? = destination.nextNode
    //                while (node != null) {
    //                    destination.heap.putAll(node.heap)
    //
    //                    node = node.nextNode
    //                }
    //
    //                for (i in destination.heap.map { it.value.value }) {
    //                    i?.bigNode = destination
    //                }
    //            }
    //        }
    //    }
    //
    //    @Test
    //    @Nightly
    //    fun `load test - collapseTo - loop method - pyramid range case`() {
    //        repeatToAverage {
    //            pyramidRangeExecutor { memory, destination ->
    //                // Unlink
    //                destination.nextNode!!.previousNode = null
    //
    //                var node: BigNode? = memory.lastNode
    //                while (node != null) {
    //                    for ((i, cell) in node.heap) {
    //                        // Remove the node from previous.
    //                        var node2: BigNode? = node.previousNode
    //                        while (node2 != null) {
    //                            node2.heap.remove(i)
    //
    //                            node2 = node2.previousNode
    //                        }
    //
    //                        // Set in destination.
    //                        destination.heap[i] = cell
    //                        cell.value?.bigNode = destination
    //                    }
    //
    //                    node = node.previousNode
    //                }
    //            }
    //        }
    //    }
    //
    //    // AUXILIARY METHODS ------------------------------------------------------
    //
    //    private fun repeatToAverage(fn: () -> Double): Double {
    //        var accumulator = 0.0
    //        for (i in 0 until repetitions) {
    //            Logger.info("Repetition ${i + 1}") {
    //                showDate = true
    //            }
    //            Logger.debug("--------------------------------------") {
    //                showDate = true
    //            }
    //
    //            accumulator += fn()
    //
    //            Logger.debug("--------------------------------------") {
    //                showDate = true
    //            }
    //        }
    //
    //        val average = accumulator / repetitions
    //
    //        Logger.info("Finished in average: $average") {
    //            showDate = true
    //        }
    //
    //        Logger.debug("--------------------------------------") {
    //            showDate = true
    //        }
    //
    //        return average
    //    }
    //
    //    private fun sameRangeExecutor(fn: (memory: LexemMemory, destination: BigNode) -> Unit): Double {
    //        val memory = TestUtils.generateTestMemory()
    //        val destination = memory.firstNode
    //
    //        Logger.debug("Creating the bigNodes") {
    //            showDate = true
    //        }
    //
    //        val creationTime = TimeUtils.measureTimeSeconds {
    //            // Add values.
    //            for (i in 0 until heapSizeSameRangeTest) {
    //                val value = LxmObject(memory)
    //                memory.add(value)
    //            }
    //
    //            // Generate the next levels.
    //            for (level in 1 until heapSizeSameRangeTest / 100) {
    //                memory.freezeCopy()
    //
    //                for (i in 0 until heapSizeSameRangeTest) {
    //                    memory.get(LxmReference(i), toWrite = true)
    //                }
    //            }
    //        }
    //
    //        Logger.debug("Creating the bigNodes - Finished in $creationTime seconds") {
    //            showDate = true
    //        }
    //
    //        Logger.debug("Collapsing the bigNodes") {
    //            showDate = true
    //        }
    //
    //        val collapsingTime = TimeUtils.measureTimeSeconds {
    //            fn(memory, destination)
    //        }
    //
    //        Logger.debug("Collapsing the bigNodes - Finished in $collapsingTime seconds") {
    //            showDate = true
    //        }
    //
    //        Logger.debug("Verifying the bigNodes") {
    //            showDate = true
    //        }
    //
    //        Assertions.assertEquals(heapSizeSameRangeTest, destination.heap.size, "The heap size property is incorrect")
    //
    //        val checkTime = TimeUtils.measureTimeSeconds {
    //            for (i in 0 until heapSizeSameRangeTest) {
    //                val cell = destination.heap[i]!!
    //                Assertions.assertEquals(destination, cell.value!!.bigNode, "The bigNode property of [$i] is incorrect")
    //            }
    //        }
    //
    //        Logger.debug("Verifying the bigNodes - Finished in $checkTime seconds") {
    //            showDate = true
    //        }
    //
    //        return collapsingTime
    //    }
    //
    //    private fun pyramidRangeExecutor(fn: (memory: LexemMemory, destination: BigNode) -> Unit): Double {
    //        val memory = TestUtils.generateTestMemory()
    //        val destination = memory.firstNode
    //
    //        Logger.debug("Creating the bigNodes") {
    //            showDate = true
    //        }
    //
    //        val creationTime = TimeUtils.measureTimeSeconds {
    //            // Add values.
    //            for (i in 0 until heapSizePyramidTest) {
    //                val value = LxmObject(memory)
    //                memory.add(value)
    //            }
    //
    //            // Generate the next levels.
    //            for (level in (0 until (heapSizePyramidTest / 100)).map { it + 1 }) {
    //                memory.freezeCopy()
    //
    //                for (i in 0 until heapSizePyramidTest - 100) {
    //                    memory.get(LxmReference(level * 100 + i), toWrite = true)
    //                }
    //
    //                for (i in 0 until 100) {
    //                    val value = LxmObject(memory)
    //                    memory.add(value)
    //                }
    //            }
    //        }
    //
    //        Logger.debug("Creating the bigNodes - Finished in $creationTime seconds") {
    //            showDate = true
    //        }
    //
    //        Logger.debug("Collapsing the bigNodes") {
    //            showDate = true
    //        }
    //
    //        val collapsingTime = TimeUtils.measureTimeSeconds {
    //            fn(memory, destination)
    //        }
    //
    //        Logger.debug("Collapsing the bigNodes - Finished in $collapsingTime seconds") {
    //            showDate = true
    //        }
    //
    //        Logger.debug("Verifying the bigNodes") {
    //            showDate = true
    //        }
    //
    //        val maxHeapSize = 2 * heapSizePyramidTest
    //        Assertions.assertEquals(maxHeapSize, destination.heap.size, "The heap size property is incorrect")
    //
    //        val checkTime = TimeUtils.measureTimeSeconds {
    //            for (i in 0 until maxHeapSize) {
    //                val cell = destination.heap[i]!!
    //                Assertions.assertEquals(destination, cell.value!!.bigNode, "The bigNode property of [$i] is incorrect")
    //            }
    //        }
    //
    //        Logger.debug("Verifying the bigNodes - Finished in $checkTime seconds") {
    //            showDate = true
    //        }
    //
    //        return collapsingTime
    //    }
}
