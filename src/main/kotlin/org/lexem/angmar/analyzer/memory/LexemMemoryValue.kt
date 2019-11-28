package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The common part of every value in lexem.
 */
internal interface LexemMemoryValue {
    /**
     * Gets the type of the value.
     */
    fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, AnyType.TypeName) as LxmReference
    }

    /**
     * Gets the type of the value as a [LxmObject].
     */
    fun getTypeAsObject(memory: LexemMemory) = getType(memory).dereferenceAs<LxmObject>(memory)!!

    /**
     * Gets the prototype of the value.
     */
    fun getPrototype(memory: LexemMemory) = getType(memory).dereferenceAs<LxmObject>(memory)!!.getPropertyValue(memory,
            AnalyzerCommons.Identifiers.Prototype) as LxmReference

    /**
     * Gets the prototype of the value as a [LxmObject].
     */
    fun getPrototypeAsObject(memory: LexemMemory) = getPrototype(memory).dereferenceAs<LxmObject>(memory)!!

    /**
     * Returns the current object if it is a [LxmObject] or the prototype otherwise.
     */
    fun getObjectOrPrototype(memory: LexemMemory) = if (this is LxmObject) {
        this
    } else {
        getPrototypeAsObject(memory)
    }
}
