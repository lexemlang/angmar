package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class FunctionCallNamedArgumentAnalyzerTest {
    @Test
    fun test() {
        val key = "test"
        val value = 123
        val text = "$key${FunctionCallNamedArgumentNode.relationalToken}$value"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = FunctionCallNamedArgumentNode.Companion::parse)

        // Prepare stack
        val args = LxmArguments(analyzer.memory)
        val argsRef = analyzer.memory.add(args)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, argsRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments) as? LxmReference ?: throw Error(
                        "The result must be a LxmReference")
        Assertions.assertEquals(argsRef, resultRef, "The resultRef is incorrect")

        val result = resultRef.dereferenceAs<LxmArguments>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmArguments")
        val namedArguments =
                result.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.ArgumentsNamed,
                        toWrite = false) ?: throw Error(
                        "The ${AnalyzerCommons.Identifiers.ArgumentsNamed} must be a LxmObject")

        val argument = namedArguments.getDereferencedProperty<LxmInteger>(analyzer.memory, key, toWrite = false)
                ?: throw Error("The $key must be a LxmInteger")

        Assertions.assertEquals(value, argument.primitive, "The primitive property is incorrect")

        // Remove Arguments from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
