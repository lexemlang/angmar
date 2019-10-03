package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The common part of every value in lexem.
 */
internal interface LexemMemoryValue {
    /**
     * Gets the type of the value.
     */
    fun getType(memory: LexemMemory) = AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, AnyType.TypeName)

    /**
     * Gets the prototype of the value.
     */
    fun getPrototype(memory: LexemMemory) =
            getType(memory).getDereferencedProperty<LxmObject>(memory, AnalyzerCommons.Identifiers.Prototype)!!

    /**
     * Returns the current object if it is a [LxmObject] or the prototype otherwise.
     */
    fun getObjectOrPrototype(memory: LexemMemory) = if (this is LxmObject) {
        this
    } else {
        getPrototype(memory)
    }
}
