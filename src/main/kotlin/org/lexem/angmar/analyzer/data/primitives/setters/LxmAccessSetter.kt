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

    constructor(memory: LexemMemory, context: LxmContext, variableName: String, node: CompiledNode,
            nodeElement: CompiledNode) {
        this.context = context.getPrimitive()
        this.variableName = variableName
        this.node = node
        this.nodeElement = nodeElement

        this.context.increaseReferences(memory.lastNode)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getSetterPrimitive(bigNode: BigNode): LexemPrimitive {
        val ctxObject = context.dereferenceAs<LxmObject>(bigNode, toWrite = false)!!

        return ctxObject.getPropertyValue(variableName) ?: throw AngmarAnalyzerException(
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

    override fun setSetterValue(bigNode: BigNode, value: LexemMemoryValue) {
        val ctxObject = context.dereferenceAs<LxmObject>(bigNode, toWrite = true)!!
        ctxObject.setPropertyAsContext(variableName, value)
    }

    override fun increaseReferences(bigNode: BigNode) {
        context.increaseReferences(bigNode)
    }

    override fun decreaseReferences(bigNode: BigNode) {
        context.decreaseReferences(bigNode)
    }

    override fun spatialGarbageCollect(gcFifo: GarbageCollectorFifo) {
        context.spatialGarbageCollect(gcFifo)
    }

    override fun toString() = "[Setter - Access] (context: $context, variableName: $variableName)"
}
