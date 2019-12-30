package org.lexem.angmar.analyzer.stdlib.globals

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Built-in Analyzer global object.
 */
internal object AnalyzerGlobalObject {
    const val ObjectName = "Analyzer"
    const val RootNode = "rootNode"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the global object.
     */
    fun initObject(memory: LexemMemory) {
        val objectValue = LxmObject(memory)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true)
                .setProperty( ObjectName, objectValue, isConstant = true)

        // Properties
        objectValue.setProperty( RootNode, LxmNil)
    }
}
