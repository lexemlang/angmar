package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * Built-in prototype of the Any object.
 */
internal object AnyPrototype {
    const val PrototypeName = "Any"

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmAnyPrototype()
        prototype.setProperty(memory, AnalyzerCommons.Operators.LogicalNot, LxmInternalFunction(::logicalNot),
                isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Performs a logical NOT of the 'this' value.
     */
    private fun logicalNot(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, AnalyzerCommons.Operators.LogicalNot,
                    LogicType.TypeName) { _: LexemAnalyzer, thisValue: LexemMemoryValue ->
                if (thisValue == LxmNil) {
                    LxmLogic.True
                } else {
                    LxmLogic.False
                }
            }
}
