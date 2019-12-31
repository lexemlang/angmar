package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class QuantifiedGroupLexemAnalyzerTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provide2SlotCombinations(): Stream<Arguments> {
            val sequence = sequence {
                val permutations = permute(listOf("x", "z")).distinct()
                for (perm in permutations) {
                    yield(Arguments.of(perm.joinToString(""), 2))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provide3SlotCombinations(): Stream<Arguments> {
            val sequence = sequence {
                var permutations = permute(listOf("x", "z", "z")).distinct()
                for (perm in permutations) {
                    yield(Arguments.of(perm.joinToString(""), 3))
                }

                permutations = permute(listOf("x", "y", "z")).distinct()
                for (perm in permutations) {
                    yield(Arguments.of(perm.joinToString(""), 3))
                }
            }

            return sequence.asStream()
        }

        @JvmStatic
        private fun provide4SlotCombinations(): Stream<Arguments> {
            val sequence = sequence {
                val permutations = permute(listOf("x", "y", "z", "z")).distinct()
                for (perm in permutations) {
                    yield(Arguments.of(perm.joinToString(""), 4))
                }
            }

            return sequence.asStream()
        }

        private fun <T> permute(list: List<T>): List<List<T>> {
            if (list.size == 1) return listOf(list)
            val perms = mutableListOf<List<T>>()
            val sub = list[0]
            for (perm in permute(list.drop(1))) for (i in 0..perm.size) {
                val newPerm = perm.toMutableList()
                newPerm.add(i, sub)
                perms.add(newPerm)
            }
            return perms
        }
    }

    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provide2SlotCombinations", "provide3SlotCombinations", "provide4SlotCombinations")
    fun `test all combinations - match`(text: String, count: Int) {
        val option1 = "${StringNode.startToken}x${StringNode.endToken}"
        val option2 = "${StringNode.startToken}y${StringNode.endToken}"
        val qtf2 = QuantifierLexemeNode.lazyAbbreviation
        val option3 = "${StringNode.startToken}z${StringNode.endToken}"
        val qtf3 =
                "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
        val grammar =
                "${QuantifiedGroupLexemeNode.startToken}$option1${QuantifiedGroupLexemeNode.patternToken}$option2${QuantifiedGroupLexemeNode.patternToken}$qtf2 $option3${QuantifiedGroupLexemeNode.patternToken}$qtf3${QuantifiedGroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                parserFunction = QuantifiedGroupLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = count + 1)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @ValueSource(strings = ["x", "y", "z", "t", ""])
    fun `test all combinations - not match`(text: String) {
        val option1 = "${StringNode.startToken}x${StringNode.endToken}"
        val option2 = "${StringNode.startToken}y${StringNode.endToken}"
        val qtf2 = QuantifierLexemeNode.lazyAbbreviation
        val option3 = "${StringNode.startToken}z${StringNode.endToken}"
        val qtf3 =
                "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
        val grammar =
                "${QuantifiedGroupLexemeNode.startToken}$option1${QuantifiedGroupLexemeNode.patternToken}$option2${QuantifiedGroupLexemeNode.patternToken}$qtf2 $option3${QuantifiedGroupLexemeNode.patternToken}$qtf3${QuantifiedGroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                parserFunction = QuantifiedGroupLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provide2SlotCombinations", "provide3SlotCombinations")
    fun `test all combinations with main quantifier - match`(text: String, count: Int) {
        val option1 = "${StringNode.startToken}x${StringNode.endToken}"
        val option2 = "${StringNode.startToken}y${StringNode.endToken}"
        val qtf2 = QuantifierLexemeNode.lazyAbbreviation
        val option3 = "${StringNode.startToken}z${StringNode.endToken}"
        val qtf3 =
                "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
        val mainQtf =
                "${QuantifiedGroupModifierNode.startToken}${QuantifiedGroupModifierNode.elementSeparator}3${QuantifiedGroupModifierNode.endToken}"
        val grammar =
                "${QuantifiedGroupLexemeNode.startToken}$mainQtf$option1${QuantifiedGroupLexemeNode.patternToken}$option2${QuantifiedGroupLexemeNode.patternToken}$qtf2 $option3${QuantifiedGroupLexemeNode.patternToken}$qtf3${QuantifiedGroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                parserFunction = QuantifiedGroupLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = count + 1)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")
        Assertions.assertEquals(text.length, analyzer.text.currentPosition(),
                "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @ValueSource(strings = ["xyzz", "yzxz"])
    fun `test all combinations with main quantifier - match not all`(text: String) {
        val option1 = "${StringNode.startToken}x${StringNode.endToken}"
        val option2 = "${StringNode.startToken}y${StringNode.endToken}"
        val qtf2 = QuantifierLexemeNode.lazyAbbreviation
        val option3 = "${StringNode.startToken}z${StringNode.endToken}"
        val qtf3 =
                "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
        val mainQtf =
                "${QuantifiedGroupModifierNode.startToken}${QuantifiedGroupModifierNode.elementSeparator}3${QuantifiedGroupModifierNode.endToken}"
        val grammar =
                "${QuantifiedGroupLexemeNode.startToken}$mainQtf$option1${QuantifiedGroupLexemeNode.patternToken}$option2${QuantifiedGroupLexemeNode.patternToken}$qtf2 $option3${QuantifiedGroupLexemeNode.patternToken}$qtf3${QuantifiedGroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                parserFunction = QuantifiedGroupLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, bigNodeCount = 4)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")
        Assertions.assertEquals(3, analyzer.text.currentPosition(), "The lexem has not consumed the characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @MethodSource("provide2SlotCombinations", "provide3SlotCombinations", "provide4SlotCombinations")
    fun `test all combinations negated - match`(text: String, count: Int) {
        val option1 = "${StringNode.startToken}x${StringNode.endToken}"
        val option2 = "${StringNode.startToken}y${StringNode.endToken}"
        val qtf2 = QuantifierLexemeNode.lazyAbbreviation
        val option3 = "${StringNode.startToken}z${StringNode.endToken}"
        val qtf3 =
                "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
        val grammar =
                "${QuantifiedGroupLexemeNode.notOperator}${QuantifiedGroupLexemeNode.startToken}$option1${QuantifiedGroupLexemeNode.patternToken}$option2${QuantifiedGroupLexemeNode.patternToken}$qtf2 $option3${QuantifiedGroupLexemeNode.patternToken}$qtf3${QuantifiedGroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                parserFunction = QuantifiedGroupLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader, status = LexemAnalyzer.ProcessStatus.Backward,
                bigNodeCount = 0)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @ParameterizedTest
    @ValueSource(strings = ["x", "y", "z", "t", ""])
    fun `test all combinations negated - not match`(text: String) {
        val option1 = "${StringNode.startToken}x${StringNode.endToken}"
        val option2 = "${StringNode.startToken}y${StringNode.endToken}"
        val qtf2 = QuantifierLexemeNode.lazyAbbreviation
        val option3 = "${StringNode.startToken}z${StringNode.endToken}"
        val qtf3 =
                "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
        val grammar =
                "${QuantifiedGroupLexemeNode.notOperator}${QuantifiedGroupLexemeNode.startToken}$option1${QuantifiedGroupLexemeNode.patternToken}$option2${QuantifiedGroupLexemeNode.patternToken}$qtf2 $option3${QuantifiedGroupLexemeNode.patternToken}$qtf3${QuantifiedGroupLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                parserFunction = QuantifiedGroupLexemeNode.Companion::parse)
        val textReader = IOStringReader.from(text)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val node = LxmNode(analyzer.memory, "name", textReader.saveCursor())
        context.setPropertyAsContext(AnalyzerCommons.Identifiers.Node, node)

        TestUtils.processAndCheckEmpty(analyzer, textReader)

        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")
        Assertions.assertEquals(0, analyzer.text.currentPosition(), "The lexem has consumed some characters")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    @Incorrect
    fun `test incorrect main quantifier bounds - {X,}`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectQuantifierBounds) {
            val option1 = "${StringNode.startToken}x${StringNode.endToken}"
            val option2 = "${StringNode.startToken}y${StringNode.endToken}"
            val qtf2 = QuantifierLexemeNode.lazyAbbreviation
            val option3 = "${StringNode.startToken}z${StringNode.endToken}"
            val qtf3 =
                    "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
            val mainQtf =
                    "${QuantifiedGroupModifierNode.startToken}7${QuantifiedGroupModifierNode.elementSeparator}${QuantifiedGroupModifierNode.endToken}"
            val grammar =
                    "${QuantifiedGroupLexemeNode.startToken}$mainQtf$option1${QuantifiedGroupLexemeNode.patternToken}$option2${QuantifiedGroupLexemeNode.patternToken}$qtf2 $option3${QuantifiedGroupLexemeNode.patternToken}$qtf3${QuantifiedGroupLexemeNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                    parserFunction = QuantifiedGroupLexemeNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect main quantifier bounds - {,X}`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectQuantifierBounds) {
            val option1 = "${StringNode.startToken}x${StringNode.endToken}"
            val option2 = "${StringNode.startToken}y${StringNode.endToken}"
            val qtf2 = QuantifierLexemeNode.lazyAbbreviation
            val option3 = "${StringNode.startToken}z${StringNode.endToken}"
            val qtf3 =
                    "${ExplicitQuantifierLexemeNode.startToken}1${ExplicitQuantifierLexemeNode.elementSeparator}2${ExplicitQuantifierLexemeNode.endToken}"
            val mainQtf =
                    "${QuantifiedGroupModifierNode.startToken}${QuantifiedGroupModifierNode.elementSeparator}0${QuantifiedGroupModifierNode.endToken}"
            val grammar =
                    "${QuantifiedGroupLexemeNode.startToken}$mainQtf$option1${QuantifiedGroupLexemeNode.patternToken}$option2${QuantifiedGroupLexemeNode.patternToken}$qtf2 $option3${QuantifiedGroupLexemeNode.patternToken}$qtf3${QuantifiedGroupLexemeNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, isDescriptiveCode = true,
                    parserFunction = QuantifiedGroupLexemeNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
