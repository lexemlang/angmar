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
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticAffirmation,
                LxmInternalFunction(::affirmation), isConstant = true)
        prototype.setProperty(memory, AnalyzerCommons.Operators.ArithmeticNegation, LxmInternalFunction(::negation),
                isConstant = true)

        prototype.setProperty(memory, AnalyzerCommons.Identifiers.ToString, LxmInternalFunction(::toString),
                isConstant = true)

        return memory.add(prototype)
    }

    /**
     * Performs an affirmation.
     */
    private fun affirmation(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments,
                    AnalyzerCommons.Operators.ArithmeticAffirmation,
                    FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                thisValue
            }

    /**
     * Performs an negation.
     */
    private fun negation(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments,
                    AnalyzerCommons.Operators.ArithmeticNegation,
                    FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmFloat.from(-thisValue.primitive)
            }

    /**
     * Returns the textual representation of the 'this' value.
     */
    private fun toString(analyzer: LexemAnalyzer, arguments: LxmArguments, signal: Int) =
            BinaryAnalyzerCommons.executeUnitaryOperator(analyzer, arguments, AnalyzerCommons.Identifiers.ToString,
                    FloatType.TypeName) { _: LexemAnalyzer, thisValue: LxmFloat ->
                LxmString.from(thisValue.primitive.toString())
            }
}
