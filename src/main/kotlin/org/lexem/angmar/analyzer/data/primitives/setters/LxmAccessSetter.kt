package org.lexem.angmar.analyzer.data.primitives.setters

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*

/**
 * A setter for a variable.
 */
internal class LxmAccessSetter : LexemSetter {
    val context: LxmReference
    val variableName: String
    val node: ParserNode
    val nodeElement: ParserNode

    // CONSTRUCTOR ------------------------------------------------------------

    constructor(memory: LexemMemory, context: LxmReference, variableName: String, node: ParserNode,
            nodeElement: ParserNode) {
        this.context = context
        this.variableName = variableName
        this.node = node
        this.nodeElement = nodeElement

        context.increaseReferences(memory)
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getPrimitive(memory: LexemMemory): LexemPrimitive {
        val ctxObject = context.dereference(memory) as LxmObject

        return ctxObject.getPropertyValue(memory, variableName) ?: throw AngmarAnalyzerException(
                AngmarAnalyzerExceptionType.IncompatibleType,
                "The variable called \"$variableName\" does not exist in the current context") {
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

    override fun setPrimitive(memory: LexemMemory, value: LexemPrimitive) {
        val ctxObject = context.dereference(memory) as LxmObject
        ctxObject.setPropertyAsContext(memory, variableName, value)
    }

    override fun increaseReferences(memory: LexemMemory) {
        context.increaseReferences(memory)
    }

    override fun decreaseReferences(memory: LexemMemory) {
        context.decreaseReferences(memory)
    }

    override fun spatialGarbageCollect(memory: LexemMemory) {
        context.spatialGarbageCollect(memory)
    }

    override fun toString() = "LxmAccessSetter(context: $context, variableName: $variableName)"
}
