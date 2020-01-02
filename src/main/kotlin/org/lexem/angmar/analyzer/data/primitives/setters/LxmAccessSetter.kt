package org.lexem.angmar.analyzer.data.primitives.setters

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*

/**
 * A setter for a variable.
 */
internal class LxmAccessSetter : LexemSetter {
    val context: LxmReference
    val variableName: String
    val node: CompiledNode
    val nodeElement: CompiledNode

    // CONSTRUCTOR ------------------------------------------------------------

    constructor(memory: IMemory, context: LxmContext, variableName: String, node: CompiledNode,
            nodeElement: CompiledNode) {
        this.context = context.getPrimitive()
        this.variableName = variableName
        this.node = node
        this.nodeElement = nodeElement

        this.context.increaseReferences(memory)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getSetterPrimitive(memory: IMemory): LexemPrimitive {
        val ctxObject = context.dereferenceAs<LxmObject>(memory, toWrite = false)!!

        return ctxObject.getPropertyValue(memory, variableName) ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.IncompatibleType,
                "The variable called '$variableName' does not exist in the current context") {
            val fullText = node.parser.reader.readAllText()
            addSourceCode(fullText, node.parser.reader.getSource()) {
                title = Consts.Logger.codeTitle
                highlightSection(node.from.position(), node.to.position() - 1)
            }
            addSourceCode(fullText, null) {
                title = Consts.Logger.hintTitle
                highlightSection(nodeElement.from.position(), nodeElement.to.position() - 1)
                message = "This variable does not exist in the current context"
            }
        }
    }

    override fun setSetterValue(memory: IMemory, value: LexemMemoryValue) {
        val ctxObject = context.dereferenceAs<LxmObject>(memory, toWrite = true)!!
        ctxObject.setPropertyAsContext(memory, variableName, value)
    }

    override fun increaseReferences(memory: IMemory) {
        context.increaseReferences(memory)
    }

    override fun decreaseReferences(memory: IMemory) {
        context.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) {
        context.spatialGarbageCollect(gcFifo)
    }

    override fun toString() = "[Setter - Access] (context: $context, variableName: $variableName)"
}
