package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Built-in prototype of the Interval object.
 */
internal object IntervalPrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()
        return memory.add(prototype)
    }
}
