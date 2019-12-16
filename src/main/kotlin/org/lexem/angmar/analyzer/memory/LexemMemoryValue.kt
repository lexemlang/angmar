package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*

/**
 * The common part of every value in lexem.
 */
internal interface LexemMemoryValue {
    /**
     * Gets the type of the value.
     */
    fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        return context.getPropertyValue(memory, AnyType.TypeName) as LxmReference
    }

    /**
     * Gets the type of the value as a [LxmObject].
     */
    fun getTypeAsObject(memory: LexemMemory, toWrite: Boolean) =
            getType(memory).dereferenceAs<LxmObject>(memory, toWrite)!!

    /**
     * Gets the prototype of the value.
     */
    fun getPrototype(memory: LexemMemory) =
            getType(memory).dereferenceAs<LxmObject>(memory, toWrite = false)!!.getPropertyValue(memory,
                    AnalyzerCommons.Identifiers.Prototype) as LxmReference

    /**
     * Gets the prototype of the value as a [LxmObject].
     */
    fun getPrototypeAsObject(memory: LexemMemory, toWrite: Boolean) =
            getPrototype(memory).dereferenceAs<LxmObject>(memory, toWrite)!!

    /**
     * Returns the current object if it is a [LxmObject] or the prototype otherwise.
     */
    fun getObjectOrPrototype(memory: LexemMemory, toWrite: Boolean) = if (this is LxmObject) {
        this
    } else {
        getPrototypeAsObject(memory, toWrite)
    }

    /**
     * Returns the textual implementation of the value in Lexem.
     */
    fun toLexemString(memory: LexemMemory): LxmString = throw AngmarUnreachableException()
}
