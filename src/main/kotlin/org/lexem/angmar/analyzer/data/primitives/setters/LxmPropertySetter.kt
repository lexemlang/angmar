package org.lexem.angmar.analyzer.data.primitives.setters

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*

/**
 * A setter for a property.
 */
internal class LxmPropertySetter : LexemSetter {
    val obj: LexemPrimitive
    val property: String
    val node: AccessExplicitMemberNode

    // CONSTRUCTOR ------------------------------------------------------------

    constructor(obj: LexemPrimitive, property: String, node: AccessExplicitMemberNode, memory: LexemMemory) {
        this.obj = obj
        this.property = property
        this.node = node

        increaseReferenceCount(memory)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Frees the references to the components of the setter.
     */
    private fun freeReferences(memory: LexemMemory) {
        if (obj is LxmReference) {
            obj.decreaseReferenceCount(memory)
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun resolve(memory: LexemMemory): LexemPrimitive {
        val objDeref = obj.dereference(memory)

        val obj = objDeref.getObjectOrPrototype(memory)

        val res = obj.getPropertyValue(memory, property) ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.IncompatibleType,
                "Undefined property called \"$property\" in object. Actual value: $obj") {
            val fullText = node.parser.reader.readAllText()
            addSourceCode(fullText, node.parser.reader.getSource()) {
                title = Consts.Logger.hintTitle
                highlightSection(node.parent!!.from.position(), node.from.position() - 1)
                message = "Review the returned object by this expression"
            }
        }

        freeReferences(memory)
        return res
    }

    override fun set(memory: LexemMemory, value: LexemPrimitive) {
        val obj = obj.dereference(memory)

        if (obj !is LxmObject) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                    "Property accesses are only allowed on objects. Actual value: $obj") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.hintTitle
                    highlightSection(node.parent!!.from.position(), node.from.position() - 1)
                    message = "Review the returned object by this expression"
                }
            }
        }

        obj.setProperty(memory, property, value)
        freeReferences(memory)
    }

    override fun increaseReferenceCount(memory: LexemMemory) {
        if (obj is LxmReference) {
            obj.increaseReferenceCount(memory)
        }
    }

    override fun toString() = "LxmAccessSetter(obj: $obj, property: $property)"
}
