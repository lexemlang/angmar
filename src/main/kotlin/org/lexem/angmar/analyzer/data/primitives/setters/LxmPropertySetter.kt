package org.lexem.angmar.analyzer.data.primitives.setters

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * A setter for a property.
 */
internal class LxmPropertySetter : LexemSetter {
    val value: LexemPrimitive
    val property: String
    val node: CompiledNode

    // CONSTRUCTOR ------------------------------------------------------------

    constructor(memory: IMemory, value: LexemPrimitive, property: String, node: CompiledNode) {
        this.value = value.getPrimitive()
        this.property = property
        this.node = node
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getSetterPrimitive(memory: IMemory): LexemPrimitive {
        val value = value.dereference(memory, toWrite = false)
        val obj = value.getObjectOrPrototype(memory, toWrite = false)

        return obj.getPropertyValue(memory, property) ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.IncompatibleType, "Undefined property called '$property' in object.") {
            val fullText = node.parser.reader.readAllText()
            addSourceCode(fullText, node.parser.reader.getSource()) {
                title = Consts.Logger.hintTitle
                highlightSection(node.parent!!.from.position(), node.from.position() - 1)
                message = "Review the returned object by this expression"
            }
        }
    }

    override fun setSetterValue(memory: IMemory, value: LexemMemoryValue) {
        val obj = this.value.dereference(memory, toWrite = true)

        if (obj !is LxmObject) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                    "Property accesses are only allowed on objects.") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.hintTitle
                    highlightSection(node.parent!!.from.position(), node.from.position() - 1)
                    message = "Review the returned object by this expression"
                }
            }
        }

        obj.setProperty(memory, property, value)
    }

    override fun increaseReferences(memory: IMemory) {
        value.increaseReferences(memory)
    }

    override fun decreaseReferences(memory: IMemory) {
        value.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(memory: IMemory, gcFifo: GarbageCollectorFifo) {
        value.spatialGarbageCollect(memory, gcFifo)
    }

    override fun toString() = "[Setter - Property] (value: $value, property: $property)"
}
