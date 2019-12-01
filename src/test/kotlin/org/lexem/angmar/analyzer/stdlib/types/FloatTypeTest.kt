package org.lexem.angmar.analyzer.stdlib.types

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class FloatTypeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        private const val integerForFloat = 275

        @JvmStatic
        private fun provideBinaryFloats() = generateFloat(2)

        @JvmStatic
        private fun provideOctalFloats() = generateFloat(8)

        @JvmStatic
        private fun provideDecimalFloats() = generateFloat(10)

        @JvmStatic
        private fun provideHexadecimalFloats() = generateFloat(16)


        // AUX METHODS --------------------------------------------------------

        private fun generateFloat(radix: Int): Stream<Arguments> {
            val result = sequence {
                for (withExponent in 0..3) {
                    var num = integerForFloat.toString(radix) + "${NumberNode.decimalSeparator}101"
                    var numAsFloat = integerForFloat.toFloat() + 1.0f / radix + 1.0f / (radix * radix * radix)

                    numAsFloat = when (withExponent) {
                        1 -> {
                            num += "${NumberNode.hexadecimalExponentSeparator[0]}1"
                            (numAsFloat * Math.pow(radix.toDouble(), 1.0)).toFloat()
                        }
                        2 -> {
                            num += "${NumberNode.hexadecimalExponentSeparator[0]}${NumberNode.exponentPositiveSign}1"
                            (numAsFloat * Math.pow(radix.toDouble(), 1.0)).toFloat()
                        }
                        3 -> {
                            num += "${NumberNode.hexadecimalExponentSeparator[0]}${NumberNode.exponentNegativeSign}1"
                            (numAsFloat * Math.pow(radix.toDouble(), -1.0)).toFloat()
                        }
                        else -> numAsFloat
                    }

                    for (withPrefix in listOf(false, true)) {
                        if (withPrefix) {
                            when (radix) {
                                2 -> num = NumberNode.binaryPrefix + num
                                8 -> num = NumberNode.octalPrefix + num
                                10 -> num = NumberNode.decimalPrefix + num
                                16 -> num = NumberNode.hexadecimalPrefix + num
                            }
                            yield(Arguments.of(num, numAsFloat, radix))
                        } else {
                            yield(Arguments.of(num, numAsFloat, radix))
                        }
                    }
                }
            }

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideDecimalFloats")
    fun `test parse without radix`(num: String, numAsFloat: Float) {
        val fnCallArguments = listOf("${StringNode.startToken}$num${StringNode.endToken}")
        val fnCall = "${FloatType.TypeName}${AccessExplicitMemberNode.accessToken}${FloatType.Parse}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be a LxmFloat")
            Assertions.assertEquals(numAsFloat, result.primitive, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideBinaryFloats", "provideOctalFloats", "provideDecimalFloats", "provideHexadecimalFloats")
    fun `test parse with radix`(num: String, numAsFloat: Float, radix: Int) {
        val fnCallArguments = listOf("${StringNode.startToken}$num${StringNode.endToken}", radix)
        val fnCall = "${FloatType.TypeName}${AccessExplicitMemberNode.accessToken}${FloatType.Parse}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be a LxmFloat")
            Assertions.assertEquals(numAsFloat, result.primitive, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test epsilonEquals`(isOk: Boolean) {
        val value1 = if (isOk) {
            FloatType.EpsilonValue
        } else {
            4f
        }
        val value2 = FloatType.EpsilonValue / 2
        val fnCallArguments = listOf(value1, value2)
        val fnCall = "${FloatType.TypeName}${AccessExplicitMemberNode.accessToken}${FloatType.EpsilonEquals}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test parse with incorrect value type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCallArguments = listOf(4)
            val fnCall = "${FloatType.TypeName}${AccessExplicitMemberNode.accessToken}${FloatType.Parse}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test parse with incorrect radix type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCallArguments = listOf("${StringNode.startToken}2${StringNode.endToken}",
                    "${StringNode.startToken}5${StringNode.endToken}")
            val fnCall = "${FloatType.TypeName}${AccessExplicitMemberNode.accessToken}${FloatType.Parse}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test epsilonEquals with incorrect left type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value1 = LxmNil
            val value2 = FloatType.EpsilonValue / 2
            val fnCallArguments = listOf(value1, value2)
            val fnCall = "${FloatType.TypeName}${AccessExplicitMemberNode.accessToken}${FloatType.EpsilonEquals}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test epsilonEquals with incorrect right type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value1 = FloatType.EpsilonValue
            val value2 = LxmNil
            val fnCallArguments = listOf(value1, value2)
            val fnCall = "${FloatType.TypeName}${AccessExplicitMemberNode.accessToken}${FloatType.EpsilonEquals}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }
}
