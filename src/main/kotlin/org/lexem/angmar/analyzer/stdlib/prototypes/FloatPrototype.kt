package org.lexem.angmar.analyzer.stdlib.prototypes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.functional.expressions.binary.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * Built-in prototype of the Float values.
 */
internal object FloatPrototype {
    /**
     * Initiates the prototype.
     */
    fun initPrototype(memory: LexemMemory): LxmReference {
        val prototype = LxmObject()

        // Operators
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticAffirmation,
                memory.add(LxmFunction(::affirmation)), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticNegation, memory.add(LxmFunction(::negation)),
                isConstant = true)

        return memory.add(prototype)
    }

    // OPERATORS --------------------------------------------------------------

    /**
     * Performs an affirmation.
     */
    private fun affirmation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.ArithmeticAffirmation,
                    FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                thisValue
            }

    /**
     * Performs a negation.
     */
    private fun negation(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, argumentsReference.dereferenceAs(analyzer.memory)!!,
                    AnalyzerCommons.Operators.ArithmeticNegation,
                    FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmFloat.from(-thisValue.primitive)
            }
}
