package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Built-in Function type object.
 */
internal object FunctionType {
    const val TypeName = "Function"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmObject) {
        val type = LxmObject(memory)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true).setProperty(memory, TypeName, type, isConstant = true)

        // Properties
        type.setProperty(memory, AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)
    }
}
