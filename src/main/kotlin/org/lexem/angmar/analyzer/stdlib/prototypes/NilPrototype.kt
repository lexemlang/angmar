package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * Built-in prototype of the Nil object.
 */
internal object NilPrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()
        prototype.setProperty(memory, AnalyzerCommons.Identifiers.ToString, LxmInternalFunction(::toString),
                isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Returns the textual representation of the 'this' value.
     */
    private fun toString(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, AnalyzerCommons.Identifiers.ToString,
                    NilType.TypeName) { _: LexemAnalyzer, _: LxmNil ->
                LxmString.Nil
            }
}
