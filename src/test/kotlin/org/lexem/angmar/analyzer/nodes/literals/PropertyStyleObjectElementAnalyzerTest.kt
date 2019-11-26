package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class PropertyStyleObjectElementAnalyzerTest {
    @Test
    fun `test correct`() {
        val propName = "test"
        val text = "$propName${ParenthesisExpressionNode.startToken}345${ParenthesisExpressionNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectElementNode.Companion::parse)

        // Prepare stack
        val obj = LxmObject()
        val objRef = analyzer.memory.add(obj)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, objRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmReference ?: throw Error(
                        "The result must be a LxmReference")
        Assertions.assertEquals(objRef, resultRef, "The resultRef is incorrect")

        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")
        val property = result.getDereferencedProperty<LxmInteger>(analyzer.memory, propName) ?: throw Error(
                "The property must be a LxmInteger")
        Assertions.assertEquals(345, property.primitive, "The primitive property is incorrect")

        // Remove Accumulator from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    @Incorrect
    fun `test incorrect key`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val text =
                    "${EscapedExpressionNode.startToken}222${EscapedExpressionNode.endToken}${ParenthesisExpressionNode.startToken}345${ParenthesisExpressionNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectElementNode.Companion::parse)

            // Prepare stack
            val obj = LxmObject()
            val objRef = analyzer.memory.add(obj)
            analyzer.memory.addToStackAsLast(objRef)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
