package org.lexem.angmar.analyzer.stdlib.types

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*

/**
 * Built-in Integer type object.
 */
internal object IntegerType {
    const val TypeName = "Integer"

    // Properties
    const val MinValue = "MinValue"
    const val MaxValue = "MaxValue"

    // Methods
    const val Parse = "parse"

    // Method arguments
    private val ParseArgs = listOf("value", "radix")

    // METHODS ----------------------------------------------------------------

    /**
     * Initiates the type.
     */
    fun initType(memory: LexemMemory, prototype: LxmObject) {
        val type = LxmObject(memory)
        AnalyzerCommons.getCurrentContext(memory, toWrite = true).setProperty(memory, TypeName, type, isConstant = true)

        // Properties
        type.setProperty(memory, AnalyzerCommons.Identifiers.Prototype, prototype, isConstant = true)
        type.setProperty(memory, MinValue, LxmInteger.from(Int.MIN_VALUE), isConstant = true)
        type.setProperty(memory, MaxValue, LxmInteger.from(Int.MAX_VALUE), isConstant = true)

        // Methods
        type.setProperty(memory, Parse, LxmFunction(memory, ::parseFunction), isConstant = true)
    }

    /**
     * Parses a float in a given radix.
     */
    private fun parseFunction(analyzer: LexemAnalyzer, arguments: LxmArguments, function: LxmFunction,
            signal: Int): Boolean {
        val parsedArguments = arguments.mapArguments(analyzer.memory, ParseArgs)

        when (signal) {
            AnalyzerNodesCommons.signalCallFunction -> {
                val value = parsedArguments[ParseArgs[0]]!!
                var radix = parsedArguments[ParseArgs[1]]!!

                if (value !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                            "The '${TypeName}${AccessExplicitMemberNode.accessToken}$Parse' method requires the parameter called '${ParseArgs[0]}' be a ${StringType.TypeName}") {}
                }

                if (radix != LxmNil) {
                    if (radix !is LxmInteger) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError,
                                "The '${TypeName}${AccessExplicitMemberNode.accessToken}$Parse' method requires the parameter called '${ParseArgs[1]}' be an $TypeName") {}
                    }
                } else {
                    radix = LxmInteger.Num10
                }

                val reader = IOStringReader.from(value.primitive)
                val parser = LexemParser(reader)
                val grammar = NumberNode.parseAnyIntegerInSpecifiedRadix(parser, ParserNode.Companion.EmptyParserNode,
                        radix.primitive)

                if (grammar != null) {
                    val compiledGrammar = NumberCompiler.compile(CompiledNode.Companion.EmptyCompiledNode, 0, grammar)
                    compiledGrammar.analyze(analyzer, AnalyzerNodesCommons.signalStart)
                } else {
                    analyzer.memory.addToStackAsLast(LxmNil)
                }
            }
        }

        return true
    }
}
