package org.lexem.angmar.analyzer.data.primitives.setters

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*

/**
 * A setter for a variable.
 */
internal class LxmAccessSetter : LexemSetter {
    val context: LxmReference
    val variableName: String
    val node: AccessExpressionNode

    // CONSTRUCTOR ------------------------------------------------------------

    constructor(memory: LexemMemory, context: LxmReference, variableName: String, node: AccessExpressionNode) {
        this.context = context
        this.variableName = variableName
        this.node = node

        increaseReferenceCount(memory)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Frees the references to the components of the setter.
     */
    private fun freeReferences(memory: LexemMemory) {
        context.decreaseReferenceCount(memory)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun resolve(memory: LexemMemory): LexemPrimitive {
        val ctxObject = context.dereference(memory) as LxmObject
        val result = ctxObject.getPropertyValue(memory, variableName) ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.IncompatibleType,
                "The variable called \"$variableName\" does not exist in the current context") {
            val fullText = node.parser.reader.readAllText()
            addSourceCode(fullText, node.parser.reader.getSource()) {
                title = Consts.Logger.codeTitle
                highlightSection(node.from.position(), node.to.position() - 1)
            }
            addSourceCode(fullText, null) {
                title = Consts.Logger.hintTitle
                highlightSection(node.element.from.position(), node.element.to.position() - 1)
                message = "This variable does not exist in the current context"
            }
        }

        freeReferences(memory)
        return result
    }

    override fun set(memory: LexemMemory, value: LexemPrimitive) {
        val ctxObject = context.dereference(memory) as LxmObject
        ctxObject.setPropertyAsContext(memory, variableName, value)
        freeReferences(memory)
    }

    override fun increaseReferenceCount(memory: LexemMemory) {
        context.increaseReferenceCount(memory)
    }

    override fun toString() = "LxmAccessSetter(context: $context, variableName: $variableName)"
}
