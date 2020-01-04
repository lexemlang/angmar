package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*

/**
 * Built-in Node type object.
 */
internal object NodeType {
    const val TypeName = "Node"

    // Properties
    const val HiddenDefaultExpressionProperties = "${AnalyzerCommons.Identifiers.HiddenPrefix}dep"
    const val HiddenDefaultExpressionGroupProperties = "${AnalyzerCommons.Identifiers.HiddenPrefix}degp"
    const val HiddenDefaultFilterProperties = "${AnalyzerCommons.Identifiers.HiddenPrefix}dfp"
    const val HiddenDefaultFilterGroupProperties = "${AnalyzerCommons.Identifiers.HiddenPrefix}dfgp"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmObject) {
        val type = LxmObject(memory)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true).setProperty(memory, TypeName, type, isConstant = true)

        // Properties
        type.setProperty(memory, AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)

        var obj = LxmObject(memory)
        for ((key, value) in Consts.Node.defaultExpressionProperties) {
            obj.setProperty(memory, key, value)
        }
        obj.makeConstantAndNotWritable(memory)
        type.setProperty(memory, HiddenDefaultExpressionProperties, obj)

        obj = LxmObject(memory)
        for ((key, value) in Consts.Node.defaultExpressionGroupProperties) {
            obj.setProperty(memory, key, value)
        }
        obj.makeConstantAndNotWritable(memory)
        type.setProperty(memory, HiddenDefaultExpressionGroupProperties, obj)

        obj = LxmObject(memory)
        for ((key, value) in Consts.Node.defaultFilterProperties) {
            obj.setProperty(memory, key, value)
        }
        obj.makeConstantAndNotWritable(memory)
        type.setProperty(memory, HiddenDefaultFilterProperties, obj)

        obj = LxmObject(memory)
        for ((key, value) in Consts.Node.defaultFilterGroupProperties) {
            obj.setProperty(memory, key, value)
        }
        obj.makeConstantAndNotWritable(memory)
        type.setProperty(memory, HiddenDefaultFilterGroupProperties, obj)
    }
}
