package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Built-in String type object.
 */
internal object StringType {
    const val TypeName = "String"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmReference) {
        val type = LxmObject()
        val reference = memory.add(type)
        AnalyzerCommons.getCurrentContext(memory).setProperty(memory, TypeName, reference, isConstant = true)

        type.setProperty(memory, AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)
    }
}
