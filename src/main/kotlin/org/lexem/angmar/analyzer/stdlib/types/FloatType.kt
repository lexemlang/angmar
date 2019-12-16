package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import kotlin.math.*

/**
 * Built-in Float type object.
 */
internal object FloatType {
    const val TypeName = "Float"

    // Properties
    const val Infinity = "Infinity"
    const val Epsilon = "Epsilon"
    val EpsilonValue = Math.ulp(1.0f)
    const val MinValue = "MinValue"
    const val MinPositiveValue = "MinPositiveValue"
    const val MaxValue = "MaxValue"

    // Methods
    const val Parse = "parse"
    const val EpsilonEquals = "epsilonEquals"

    // Method arguments
    private val ParseArgs = listOf("value", "radix")
    private val EpsilonEqualsArgs = listOf("left", "right")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmReference) {
        val type = LxmObject(memory)
        val reference = memory.add(type)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true)
                .setProperty(memory, TypeName, reference, isConstant = true)

        // Properties
        type.setProperty(memory, AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)
        type.setProperty(memory, Infinity, LxmFloat.from(-Float.POSITIVE_INFINITY), isConstant = true)
        type.setProperty(memory, Epsilon, LxmFloat.from(EpsilonValue), isConstant = true)
        type.setProperty(memory, MinValue, LxmFloat.from(-Float.MAX_VALUE), isConstant = true)
        type.setProperty(memory, MinPositiveValue, LxmFloat.from(Float.MIN_VALUE), isConstant = true)
        type.setProperty(memory, MaxValue, LxmFloat.from(Float.MAX_VALUE), isConstant = true)

        // Methods
        type.setProperty(memory, Parse, memory.add(LxmFunction(memory, ::parseFunction)), isConstant = true)
        type.setProperty(memory, EpsilonEquals, memory.add(LxmFunction(memory, ::epsilonEqualsFunction)),
                isConstant = true)
    }

    /**
     * Parses a float in a given radix.
     */
    private fun parseFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, ParseArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val value = parsedArguments[ParseArgs[0]]!!
                var radix = parsedArguments[ParseArgs[1]]!!

                if (value !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$Parse' method requires the parameter called '${ParseArgs[0]}' be a ${StringType.TypeName}") {}
                }

                if (radix != LxmNil) {
                    if (radix !is LxmInteger) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                "The '$TypeName${AccessExplicitMemberNode.accessToken}$Parse' method requires the parameter called '${ParseArgs[1]}' be an ${IntegerType.TypeName}") {}
                    }
                } else {
                    radix = LxmInteger.Num10
                }

                val reader = IOStringReader.from(value.primitive)
                val parser = LexemParser(reader)
                val grammar = NumberNode.parseAnyNumberInSpecifiedRadix(parser, ParserNode.Companion.EmptyParserNode, 0,
                        radix.primitive)

                if (grammar != null) {
                    grammar.analyze(analyzer, AnalyzerNodesCommons.signalStart)

                    val result = analyzer.memory.getLastFromStack()
                    if (result is LxmInteger) {
                        analyzer.memory.replaceLastStackCell(LxmFloat.from(result.primitive.toFloat()))
                    }
                } else {
                    analyzer.memory.addToStackAsLast(LxmNil)
                }
            }
        }

        return true
    }

    /**
     * Compares the equality of two float values ignoring distances lower or equal than the Epsilon between them.
     */
    private fun epsilonEqualsFunction(analyzer: LexemAnalyzer, argumentsReference: LxmReference, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments =
                argumentsReference.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false)!!.mapArguments(
                        analyzer.memory, EpsilonEqualsArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val left = parsedArguments[EpsilonEqualsArgs[0]]!!
                val right = parsedArguments[EpsilonEqualsArgs[1]]!!

                val value1 = when (left) {
                    is LxmFloat -> left.primitive.toDouble()
                    is LxmInteger -> left.primitive.toDouble()
                    else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$EpsilonEquals' method requires the parameter called '${EpsilonEqualsArgs[0]}' be a $TypeName or ${IntegerType.TypeName}") {}
                }

                val value2 = when (right) {
                    is LxmFloat -> right.primitive.toDouble()
                    is LxmInteger -> right.primitive.toDouble()
                    else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '$TypeName${AccessExplicitMemberNode.accessToken}$EpsilonEquals' method requires the parameter called '${EpsilonEqualsArgs[1]}' be a $TypeName or ${IntegerType.TypeName}") {}
                }

                analyzer.memory.addToStackAsLast(LxmLogic.from(abs(value1 - value2) <= EpsilonValue))
            }
        }

        return true
    }
}
