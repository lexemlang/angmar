package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [LexemPropertyPostfixNode].
 */
internal class LexemePropertyPostfixCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: LexemPropertyPostfixNode) : CompiledNode(parent, parentSignal, parserNode) {
    val properties = mutableMapOf<String, PropertyValue>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = throw AngmarUnreachableException()

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int,
                node: LexemPropertyPostfixNode): LexemePropertyPostfixCompiled {
            val result = LexemePropertyPostfixCompiled(parent, parentSignal, node)

            // Init properties.
            result.properties[LexemPropertyPostfixNode.reversedProperty] = PropertyValue.Inherit
            result.properties[LexemPropertyPostfixNode.insensibleProperty] = PropertyValue.Inherit

            // Process positives.
            for (propertyName in node.positiveElements) {
                result.properties[propertyName] = PropertyValue.True
            }

            // Process negatives.
            for (propertyName in node.negativeElements) {
                result.properties[propertyName] = PropertyValue.False
            }

            // Process reversed.
            for (propertyName in node.reversedElements) {
                result.properties[propertyName] = PropertyValue.Reverse
            }

            return result
        }

        /**
         * The property value that indicates a constant or an inheritance type.
         */
        enum class PropertyValue {
            True,
            False,
            Reverse,
            Inherit
        }
    }
}
