package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Built-in prototype of the Object object.
 */
internal object ObjectPrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory, anyPrototypeReference: LxmReference): LxmReference {
        val prototype = LxmObject(anyPrototypeReference, memory)

        return memory.add(prototype)
    }
}
