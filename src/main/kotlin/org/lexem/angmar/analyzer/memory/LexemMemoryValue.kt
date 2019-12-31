package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.errors.*

/**
 * The common part of every value in lexem.
 */
internal interface LexemMemoryValue {
    /**
     * Gets the primitive associated to this memory value.
     */
    fun getPrimitive(): LexemPrimitive

    /**
     * Dereferences all indirect references until get a value that is not a [LxmReference] or [LexemSetter].
     */
    fun dereference(bigNode: BigNode, toWrite: Boolean): LexemMemoryValue = this

    /**
     * Dereferences all indirect references until get a value that is not a [LxmReference] or [LexemSetter].
     */
    fun dereference(memory: LexemMemory, toWrite: Boolean) = dereference(memory.lastNode, toWrite)

    /**
     * Gets the type of the value.
     */
    fun getType(bigNode: BigNode): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(bigNode, toWrite = false)
        return context.getPropertyValue(AnyType.TypeName) as LxmReference
    }

    /**
     * Gets the type of the value.
     */
    fun getType(memory: LexemMemory) = getType(memory.lastNode)

    /**
     * Gets the type of the value as a [LxmObject].
     */
    fun getTypeAsObject(bigNode: BigNode, toWrite: Boolean) =
            getType(bigNode).dereferenceAs<LxmObject>(bigNode, toWrite)!!

    /**
     * Gets the prototype of the value.
     */
    fun getPrototype(bigNode: BigNode) =
            getType(bigNode).dereferenceAs<LxmObject>(bigNode, toWrite = false)!!.getPropertyValue(
                    AnalyzerCommons.Identifiers.Prototype) as LxmReference

    /**
     * Gets the prototype of the value as a [LxmObject].
     */
    fun getPrototypeAsObject(bigNode: BigNode, toWrite: Boolean) =
            getPrototype(bigNode).dereferenceAs<LxmObject>(bigNode, toWrite)!!

    /**
     * Gets the prototype of the value as a [LxmObject].
     */
    fun getPrototypeAsObject(memory: LexemMemory, toWrite: Boolean) = getPrototypeAsObject(memory.lastNode, toWrite)

    /**
     * Returns the current object if it is a [LxmObject] or the prototype otherwise.
     */
    fun getObjectOrPrototype(bigNode: BigNode, toWrite: Boolean) = if (this is LxmObject) {
        this
    } else {
        getPrototypeAsObject(bigNode, toWrite)
    }

    /**
     * Returns the current object if it is a [LxmObject] or the prototype otherwise.
     */
    fun getObjectOrPrototype(memory: LexemMemory, toWrite: Boolean) = getObjectOrPrototype(memory.lastNode, toWrite)

    /**
     * Returns the textual implementation of the value in Lexem.
     */
    fun toLexemString(bigNode: BigNode): LxmString = throw AngmarUnreachableException()

    /**
     * Returns the textual implementation of the value in Lexem.
     */
    fun toLexemString(memory: LexemMemory) = toLexemString(memory.lastNode)

    /**
     * Collects all the garbage of the current big node.
     */
    fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) = Unit
}
