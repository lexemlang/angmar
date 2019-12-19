package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Built-in prototype of the Node object.
 */
internal object NodePrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmObject {
        val prototype = LxmObject(memory)
        return prototype
    }
}
