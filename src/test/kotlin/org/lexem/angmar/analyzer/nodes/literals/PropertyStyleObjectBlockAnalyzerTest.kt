package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class PropertyStyleObjectBlockAnalyzerTest {
    val positivePropNames = listOf("pa", "pb", "pc")
    val negativePropNames = listOf("na", "nb", "nc")
    val setProps = listOf(Pair("sa", 1), Pair("sb", 2), Pair("sc", 3))

    val positivePropNamesText = positivePropNames.joinToString(" ")
    val negativePropNamesText = negativePropNames.joinToString(" ")
    val setPropsText = setProps.joinToString(
            " ") { "${it.first}${ParenthesisExpressionNode.startToken}${it.second}${ParenthesisExpressionNode.endToken}" }

    // TESTS ------------------------------------------------------------------

    @Test
    fun `test positives`() {
        val text =
                "${PropertyStyleObjectBlockNode.startToken}$positivePropNamesText${PropertyStyleObjectBlockNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        for (propName in positivePropNames) {
            val property = result.getPropertyValue(analyzer.memory, propName)
            Assertions.assertEquals(LxmLogic.True, property, "The property [$propName] is incorrect")
        }

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test negatives`() {
        val text =
                "${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.negativeToken}$negativePropNamesText${PropertyStyleObjectBlockNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        for (propName in negativePropNames) {
            val property = result.getPropertyValue(analyzer.memory, propName)
            Assertions.assertEquals(LxmLogic.False, property, "The property [$propName] is incorrect")
        }

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test sets`() {
        val text =
                "${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.setToken}$setPropsText${PropertyStyleObjectBlockNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        for (prop in setProps) {
            val property = result.getPropertyValue(analyzer.memory, prop.first) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(prop.second, property.primitive, "The property [${prop.first}] is incorrect")
        }

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test positives and negatives`() {
        val text =
                "${PropertyStyleObjectBlockNode.startToken}$positivePropNamesText ${PropertyStyleObjectBlockNode.negativeToken}$negativePropNamesText${PropertyStyleObjectBlockNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        for (propName in positivePropNames) {
            val property = result.getPropertyValue(analyzer.memory, propName)
            Assertions.assertEquals(LxmLogic.True, property, "The property [$propName] is incorrect")
        }

        for (propName in negativePropNames) {
            val property = result.getPropertyValue(analyzer.memory, propName)
            Assertions.assertEquals(LxmLogic.False, property, "The property [$propName] is incorrect")
        }

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test positives and sets`() {
        val text =
                "${PropertyStyleObjectBlockNode.startToken}$positivePropNamesText${PropertyStyleObjectBlockNode.setToken}$setPropsText${PropertyStyleObjectBlockNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        for (propName in positivePropNames) {
            val property = result.getPropertyValue(analyzer.memory, propName)
            Assertions.assertEquals(LxmLogic.True, property, "The property [$propName] is incorrect")
        }

        for (prop in setProps) {
            val property = result.getPropertyValue(analyzer.memory, prop.first) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(prop.second, property.primitive, "The property [${prop.first}] is incorrect")
        }

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test negatives and sets`() {
        val text =
                "${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.negativeToken}$negativePropNamesText${PropertyStyleObjectBlockNode.setToken}$setPropsText${PropertyStyleObjectBlockNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        for (propName in negativePropNames) {
            val property = result.getPropertyValue(analyzer.memory, propName)
            Assertions.assertEquals(LxmLogic.False, property, "The property [$propName] is incorrect")
        }

        for (prop in setProps) {
            val property = result.getPropertyValue(analyzer.memory, prop.first) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(prop.second, property.primitive, "The property [${prop.first}] is incorrect")
        }

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test positives, negatives and sets`() {
        val text =
                "${PropertyStyleObjectBlockNode.startToken}$positivePropNamesText ${PropertyStyleObjectBlockNode.negativeToken}$negativePropNamesText${PropertyStyleObjectBlockNode.setToken}$setPropsText${PropertyStyleObjectBlockNode.endToken}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef = analyzer.memory.popStack() as? LxmReference ?: throw Error("The result must be a LxmReference")
        val result =
                resultRef.dereferenceAs<LxmObject>(analyzer.memory) ?: throw Error("The result must be a LxmObject")

        for (propName in positivePropNames) {
            val property = result.getPropertyValue(analyzer.memory, propName)
            Assertions.assertEquals(LxmLogic.True, property, "The property [$propName] is incorrect")
        }

        for (propName in negativePropNames) {
            val property = result.getPropertyValue(analyzer.memory, propName)
            Assertions.assertEquals(LxmLogic.False, property, "The property [$propName] is incorrect")
        }

        for (prop in setProps) {
            val property = result.getPropertyValue(analyzer.memory, prop.first) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(prop.second, property.primitive, "The property [${prop.first}] is incorrect")
        }

        // Decrease the reference count.
        resultRef.decreaseReferenceCount(analyzer.memory)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    @Incorrect
    fun `test incorrect positive`() {
        TestUtils.assertAnalyzerException {
            val text =
                    "${PropertyStyleObjectBlockNode.startToken}${EscapedExpressionNode.startToken}222${EscapedExpressionNode.endToken}${PropertyStyleObjectBlockNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect negative`() {
        TestUtils.assertAnalyzerException {
            val text =
                    "${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.negativeToken}${EscapedExpressionNode.startToken}222${EscapedExpressionNode.endToken}${PropertyStyleObjectBlockNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = PropertyStyleObjectBlockNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
