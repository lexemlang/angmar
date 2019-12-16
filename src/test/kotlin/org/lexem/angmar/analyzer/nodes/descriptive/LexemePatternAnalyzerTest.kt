package org.lexem.angmar.analyzer.nodes.descriptive

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LexemePatternAnalyzerTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun providePatternTypes(): Stream<Arguments> {
            val patterns = listOf(LexemePatternNode.Companion.PatternType.Alternative,
                    LexemePatternNode.Companion.PatternType.Additive, LexemePatternNode.Companion.PatternType.Selective)
            val sequence = sequence {
                for (type in patterns) {
                    yield(Arguments.of(type))
                }
            }

            return sequence.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @Test
    fun `test matching static pattern`() {
        val text = "c"
        val grammar = generatePattern(text, LexemePatternNode.Companion.PatternType.Static)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test not matching static pattern`() {
        val text = "c"
        val grammar = generatePattern("a", LexemePatternNode.Companion.PatternType.Static)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test matching optional pattern`() {
        val text = "c"
        val grammar = generatePattern(text, LexemePatternNode.Companion.PatternType.Optional)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test not matching optional pattern`() {
        val text = "c"
        val grammar = generatePattern("a", LexemePatternNode.Companion.PatternType.Optional)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test matching negative pattern`() {
        val text = "c"
        val grammar = generatePattern(text, LexemePatternNode.Companion.PatternType.Negative)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has consumed some characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test not matching negative pattern`() {
        val text = "c"
        val grammar = generatePattern("a", LexemePatternNode.Companion.PatternType.Negative)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("providePatternTypes")
    fun `test matching pattern without union`(type: LexemePatternNode.Companion.PatternType) {
        val text = "c"
        val unionName = "union"
        val grammar = generatePattern(text, unionName, type)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        val union = unions.getDereferencedProperty<LxmPatternUnion>(analyzer.memory, unionName, toWrite = false)
                ?: throw Error("The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")

        val quantifier = when (type) {
            LexemePatternNode.Companion.PatternType.Additive -> LxmQuantifier.AdditivePattern
            LexemePatternNode.Companion.PatternType.Selective -> LxmQuantifier.SelectivePattern
            LexemePatternNode.Companion.PatternType.Alternative -> LxmQuantifier.AlternativePattern
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @ParameterizedTest
    @MethodSource("providePatternTypes")
    fun `test not matching pattern without union`(type: LexemePatternNode.Companion.PatternType) {
        val text = "c"
        val unionName = "union"
        val grammar = generatePattern("a", unionName, type)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        val union = unions.getDereferencedProperty<LxmPatternUnion>(analyzer.memory, unionName, toWrite = false)
                ?: throw Error("The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(0, unionIndex.primitive, "The union index is incorrect")

        val quantifier = when (type) {
            LexemePatternNode.Companion.PatternType.Additive -> LxmQuantifier.AdditivePattern
            LexemePatternNode.Companion.PatternType.Selective -> LxmQuantifier.SelectivePattern
            LexemePatternNode.Companion.PatternType.Alternative -> LxmQuantifier.AlternativePattern
            else -> throw AngmarUnreachableException()
        }

        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @ParameterizedTest
    @MethodSource("providePatternTypes")
    fun `test matching pattern with union`(type: LexemePatternNode.Companion.PatternType) {
        val text = "c"
        val unionName = "union"
        val grammar = generatePattern(text, unionName, type)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        val quantifier = when (type) {
            LexemePatternNode.Companion.PatternType.Additive -> LxmQuantifier.AdditivePattern
            LexemePatternNode.Companion.PatternType.Selective -> LxmQuantifier.SelectivePattern
            LexemePatternNode.Companion.PatternType.Alternative -> LxmQuantifier.AlternativePattern
            else -> throw AngmarUnreachableException()
        }
        var unions = LxmObject(analyzer.memory)
        var union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)
        unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        union = unions.getDereferencedProperty(analyzer.memory, unionName, toWrite = false) ?: throw Error(
                "The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @ParameterizedTest
    @MethodSource("providePatternTypes")
    fun `test not matching pattern with union`(type: LexemePatternNode.Companion.PatternType) {
        val text = "c"
        val unionName = "union"
        val grammar = generatePattern("a", unionName, type)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        val quantifier = when (type) {
            LexemePatternNode.Companion.PatternType.Additive -> LxmQuantifier.AdditivePattern
            LexemePatternNode.Companion.PatternType.Selective -> LxmQuantifier.SelectivePattern
            LexemePatternNode.Companion.PatternType.Alternative -> LxmQuantifier.AlternativePattern
            else -> throw AngmarUnreachableException()
        }
        var unions = LxmObject(analyzer.memory)
        var union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)
        unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        union = unions.getDereferencedProperty(analyzer.memory, unionName, toWrite = false) ?: throw Error(
                "The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(0, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @Test
    fun `test matching alternative pattern with finished union`() {
        val text = "c"
        val unionName = "union"
        val grammar = generatePattern(text, unionName, LexemePatternNode.Companion.PatternType.Alternative)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        val quantifier = LxmQuantifier.AlternativePattern
        var unions = LxmObject(analyzer.memory)
        var union = LxmPatternUnion(quantifier, LxmInteger.Num1, analyzer.memory)
        unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        union = unions.getDereferencedProperty(analyzer.memory, unionName, toWrite = false) ?: throw Error(
                "The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @Test
    fun `test matching quantified pattern without union`() {
        val text = "c"
        val unionName = "union"
        val quantifier = LxmQuantifier(2, 4)
        val grammar = generateQuantifiedPatterns(text, unionName, quantifier)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        val union = unions.getDereferencedProperty<LxmPatternUnion>(analyzer.memory, unionName, toWrite = false)
                ?: throw Error("The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @Test
    fun `test not matching quantified pattern without union`() {
        val text = "c"
        val unionName = "union"
        val quantifier = LxmQuantifier(2, 4)
        val grammar = generateQuantifiedPatterns("a", unionName, quantifier)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        val union = unions.getDereferencedProperty<LxmPatternUnion>(analyzer.memory, unionName, toWrite = false)
                ?: throw Error("The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(0, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @Test
    fun `test matching quantified pattern with union`() {
        val text = "c"
        val unionName = "union"
        val quantifier = LxmQuantifier(2, 4)
        val grammar = generateQuantifiedPatterns(text, unionName, quantifier)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        var union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)
        unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        union = unions.getDereferencedProperty(analyzer.memory, unionName, toWrite = false) ?: throw Error(
                "The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @Test
    fun `test not matching quantified pattern with union`() {
        val text = "c"
        val unionName = "union"
        val quantifier = LxmQuantifier(2, 4)
        val grammar = generateQuantifiedPatterns("a", unionName, quantifier)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        var union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)
        unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        union = unions.getDereferencedProperty(analyzer.memory, unionName, toWrite = false) ?: throw Error(
                "The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(0, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @Test
    fun `test matching quantified pattern with finished union`() {
        val text = "c"
        val unionName = "union"
        val quantifier = LxmQuantifier(2, 4)
        val grammar = generateQuantifiedPatterns(text, unionName, quantifier)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        var union = LxmPatternUnion(quantifier, LxmInteger.from(quantifier.max), analyzer.memory)
        unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        union = unions.getDereferencedProperty(analyzer.memory, unionName, toWrite = false) ?: throw Error(
                "The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(quantifier.max, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }


    /// ----


    @Test
    fun `test matching anonymous quantified pattern with union`() {
        val text = "c"
        val unionName = "union"
        val quantifier = LxmQuantifier(2, 4)
        val grammar = generateQuantifiedPatterns(text, unionName, null)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        var union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)
        unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 2)

        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        union = unions.getDereferencedProperty(analyzer.memory, unionName, toWrite = false) ?: throw Error(
                "The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @Test
    fun `test not matching anonymous quantified pattern with union`() {
        val text = "c"
        val unionName = "union"
        val quantifier = LxmQuantifier(2, 4)
        val grammar = generateQuantifiedPatterns("a", unionName, null)
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        var union = LxmPatternUnion(quantifier, LxmInteger.Num0, analyzer.memory)
        unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The pattern has not consumed the characters")

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        union = unions.getDereferencedProperty(analyzer.memory, unionName, toWrite = false) ?: throw Error(
                "The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(0, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(quantifier), "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @Test
    @Incorrect
    fun `test anonymous quantified pattern without union`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.PatternUnionWithoutQuantifier) {
            val text = "c"
            val unionName = "union"
            val grammar = generateQuantifiedPatterns(text, unionName, null)
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                    isDescriptiveCode = true)
            val textReader = IOStringReader.from(text)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
            val unions = LxmObject(analyzer.memory)
            context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
            context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                    analyzer.memory.add(unions))

            TestUtils.processAndCheckEmpty(analyzer, textReader)
        }
    }

    @Test
    @Incorrect
    fun `test already existing union with other bounds`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.PatternUnionAlreadyExists) {
            val text = "c"
            val unionName = "union"
            val quantifier = LxmQuantifier(2, 4)
            val grammar = generateQuantifiedPatterns(text, unionName, quantifier)
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                    isDescriptiveCode = true)
            val textReader = IOStringReader.from(text)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
            val unions = LxmObject(analyzer.memory)
            val quantifier2 = LxmQuantifier(3)
            val union = LxmPatternUnion(quantifier2, LxmInteger.Num0, analyzer.memory)
            unions.setProperty(analyzer.memory, unionName, analyzer.memory.add(union))
            context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
            context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                    analyzer.memory.add(unions))

            TestUtils.processAndCheckEmpty(analyzer, textReader)
        }
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without tag`(keyword: String) {
        val unionName = "union"
        val grammar =
                "${LexemePatternNode.patternToken}$unionName${LexemePatternNode.unionNameRelationalToken} ${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        val union = unions.getDereferencedProperty<LxmPatternUnion>(analyzer.memory, unionName, toWrite = false)
                ?: throw Error("The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(LxmQuantifier.AlternativePattern),
                "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @ParameterizedTest
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag`(keyword: String) {
        val unionName = "union"
        val tagName = "tag"
        val blockExpression = "$keyword${BlockStmtNode.tagPrefix}$tagName"
        val grammar =
                "${LexemePatternNode.patternToken}$unionName${LexemePatternNode.unionNameRelationalToken} ${BlockStmtNode.startToken} $blockExpression ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, tagName, null) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        val union = unions.getDereferencedProperty<LxmPatternUnion>(analyzer.memory, unionName, toWrite = false)
                ?: throw Error("The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(LxmQuantifier.AlternativePattern),
                "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    @ParameterizedTest
    @ValueSource(strings = [ControlWithExpressionStmtNode.returnKeyword])
    fun `test return control signal`(keyword: String) {
        val unionName = "union"
        val value = LxmInteger.Num10
        val blockExpression = "$keyword $value"
        val grammar =
                "${LexemePatternNode.patternToken}$unionName${LexemePatternNode.unionNameRelationalToken} ${BlockStmtNode.startToken} $blockExpression ${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = LexemePatternNode.Companion::parse,
                isDescriptiveCode = true)
        val textReader = IOStringReader.from("")

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode("name", textReader.saveCursor(), null, analyzer.memory)
        var unions = LxmObject(analyzer.memory)
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, analyzer.memory.add(node))
        context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                analyzer.memory.add(unions))

        TestUtils.assertControlSignalRaisedCheckingStack(analyzer, keyword, null, value) {
            TestUtils.processAndCheckEmpty(analyzer)
        }

        // Check unions
        context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        unions = context.getDereferencedProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                toWrite = false)!!
        val union = unions.getDereferencedProperty<LxmPatternUnion>(analyzer.memory, unionName, toWrite = false)
                ?: throw Error("The union called $unionName does not exist.")
        val unionIndex = union.getIndex(analyzer.memory)

        Assertions.assertEquals(1, unionIndex.primitive, "The union index is incorrect")
        Assertions.assertTrue(union.quantifierIsEqualsTo(LxmQuantifier.AlternativePattern),
                "The quantifier is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenPatternUnions))
    }

    // AUXILIARY FUNCTIONS ----------------------------------------------------

    private fun generatePattern(text: String?,
            type: LexemePatternNode.Companion.PatternType = LexemePatternNode.Companion.PatternType.Alternative) =
            "${LexemePatternNode.patternToken}${type.token} ${printText(text)}"

    private fun generatePattern(text: String?, unionName: String,
            type: LexemePatternNode.Companion.PatternType = LexemePatternNode.Companion.PatternType.Alternative) =
            "${LexemePatternNode.patternToken}${type.token}$unionName${LexemePatternNode.unionNameRelationalToken} ${printText(
                    text)}"

    private fun generateQuantifiedPatterns(text: String?, unionName: String, quantifier: LxmQuantifier?) =
            if (quantifier != null) {
                if (quantifier.isInfinite) {
                    "${LexemePatternNode.patternToken}${ExplicitQuantifierLexemeNode.startToken}${quantifier.min}${ExplicitQuantifierLexemeNode.elementSeparator}${ExplicitQuantifierLexemeNode.endToken}$unionName${LexemePatternNode.unionNameRelationalToken} ${printText(
                            text)}"
                } else {
                    "${LexemePatternNode.patternToken}${ExplicitQuantifierLexemeNode.startToken}${quantifier.min}${ExplicitQuantifierLexemeNode.elementSeparator}${quantifier.max}${ExplicitQuantifierLexemeNode.endToken}$unionName${LexemePatternNode.unionNameRelationalToken} ${printText(
                            text)}"
                }

            } else {
                "${LexemePatternNode.patternToken}${LexemePatternNode.quantifierSlaveToken}$unionName${LexemePatternNode.unionNameRelationalToken} ${printText(
                        text)}"
            }

    private fun printText(text: String?) = if (text == null) {
        ""
    } else {
        "${StringNode.startToken}${text}${StringNode.endToken}"
    }
}
