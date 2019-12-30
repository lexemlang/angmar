package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Built-in Node type object.
 */
internal object NodeType {
    const val TypeName = "Node"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmObject) {
        val type = LxmObject(memory)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true).setProperty( TypeName, type, isConstant = true)

        // Properties
        type.setProperty( AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)
    }
}
