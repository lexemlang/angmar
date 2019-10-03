package org.lexem.angmar.analyzer.stdlib.globals

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Built-in Math global object.
 */
internal object GlobalsGlobalObject {
    const val ObjectName = "Globals"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the global object.
     */
    fun initObject(memory: LexemMemory) {
        val objectValue = LxmObject()
        val reference = memory.add(objectValue)
        AnalyzerCommons.getCurrentContext(memory).setProperty(memory, ObjectName, reference, isConstant = true)
    }
}
