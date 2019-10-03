package org.lexem.angmar.analyzer.nodes.functional.expressions.modifiers

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class FunctionCallExpressionPropertiesAnalyzerTest {
    @Test
    fun test() {
        val propName = "test"
        val text =
                "${FunctionCallExpressionPropertiesNode.relationalToken}${PropertyStyleObjectBlockNode.startToken}$propName${PropertyStyleObjectBlockNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text,
                parserFunction = FunctionCallExpressionPropertiesNode.Companion::parse)

        // Prepare stack
        val args = LxmArguments(analyzer.memory)
        val argsRef = analyzer.memory.add(args)
        analyzer.memory.pushStack(argsRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        Assertions.assertEquals(argsRef, resultRef, "The resultRef is incorrect")

        val result = resultRef.dereferenceAs<LxmArguments>(analyzer.memory) ?: throw Error(
                "The result must be a LxmArguments")
        val namedArguments =
                result.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.ArgumentsNamed)
                        ?: throw Error("The ${AnalyzerCommons.Identifiers.ArgumentsNamed} must be a LxmObject")

        val argument = namedArguments.getDereferencedProperty<LxmObject>(analyzer.memory,
                AnalyzerCommons.Identifiers.ArgumentsProperties) ?: throw Error(
                "The ${AnalyzerCommons.Identifiers.ArgumentsProperties} must be a LxmObject")

        val argumentValue = argument.getPropertyValue(analyzer.memory, propName)

        Assertions.assertEquals(LxmLogic.True, argumentValue, "The argumentValue is incorrect")

        // Decrease reference count.
        argsRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
