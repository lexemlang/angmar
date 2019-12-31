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

    constructor(value: LexemPrimitive, property: String, node: CompiledNode, memory: LexemMemory) {
        this.value = value.getPrimitive()
        this.property = property
        this.node = node

        this.value.increaseReferences(memory.lastNode)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getSetterPrimitive(bigNode: BigNode): LexemPrimitive {
        val value = value.dereference(bigNode, toWrite = false)
        val obj = value.getObjectOrPrototype(bigNode, toWrite = false)

        return obj.getPropertyValue(property) ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.IncompatibleType, "Undefined property called '$property' in object.") {
            val fullText = node.parser.reader.readAllText()
            addSourceCode(fullText, node.parser.reader.getSource()) {
                title = Consts.Logger.hintTitle
                highlightSection(node.parent!!.from.position(), node.from.position() - 1)
                message = "Review the returned object by this expression"
            }
        }
    }

    override fun setSetterValue(bigNode: BigNode, value: LexemMemoryValue) {
        val obj = this.value.dereference(bigNode, toWrite = true)

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

        obj.setProperty(property, value)
    }

    override fun increaseReferences(bigNode: BigNode) {
        value.increaseReferences(bigNode)
    }

    override fun decreaseReferences(bigNode: BigNode) {
        value.decreaseReferences(bigNode)
    }

    override fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) {
        value.spatialGarbageCollect(gcFifo)
    }

    override fun toString() = "[Setter - Property] (value: $value, property: $property)"
}
