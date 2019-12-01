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

internal class IntegerTypeTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        private const val integerPart = 27600

        @JvmStatic
        private fun provideBinaryIntegers() = generateIntegers(2)

        @JvmStatic
        private fun provideOctalIntegers() = generateIntegers(8)

        @JvmStatic
        private fun provideDecimalIntegers() = generateIntegers(10)

        @JvmStatic
        private fun provideHexadecimalIntegers() = generateIntegers(16)


        // AUX METHODS --------------------------------------------------------

        private fun generateIntegers(radix: Int): Stream<Arguments> {
            val result = sequence {
                for (withDecimals in listOf(false, true)) {
                    var num = integerPart.toString(radix)

                    if (withDecimals) {
                        num += "${NumberNode.decimalSeparator}0"
                    }

                    val numAsInt = integerPart

                    for (withPrefix in listOf(false, true)) {
                        if (withPrefix) {
                            when (radix) {
                                2 -> num = NumberNode.binaryPrefix + num
                                8 -> num = NumberNode.octalPrefix + num
                                10 -> num = NumberNode.decimalPrefix + num
                                16 -> num = NumberNode.hexadecimalPrefix + num
                            }
                            yield(Arguments.of(num, numAsInt, radix))
                        } else {
                            yield(Arguments.of(num, numAsInt, radix))
                        }
                    }
                }
            }

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideDecimalIntegers")
    fun `test parse without radix`(num: String, numAsInt: Int) {
        val fnCallArguments = listOf("${StringNode.startToken}$num${StringNode.endToken}")
        val fnCall = "${IntegerType.TypeName}${AccessExplicitMemberNode.accessToken}${IntegerType.Parse}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be a LxmInteger")
            Assertions.assertEquals(numAsInt, result.primitive, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @MethodSource("provideBinaryIntegers", "provideOctalIntegers", "provideDecimalIntegers",
            "provideHexadecimalIntegers")
    fun `test parse with radix`(num: String, numAsInt: Int, radix: Int) {
        val fnCallArguments = listOf("${StringNode.startToken}$num${StringNode.endToken}", radix)
        val fnCall = "${IntegerType.TypeName}${AccessExplicitMemberNode.accessToken}${IntegerType.Parse}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(grammar) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be a LxmInteger")
            Assertions.assertEquals(numAsInt, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test parse with incorrect value type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCallArguments = listOf(4)
            val fnCall = "${IntegerType.TypeName}${AccessExplicitMemberNode.accessToken}${IntegerType.Parse}"
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
            val fnCall = "${IntegerType.TypeName}${AccessExplicitMemberNode.accessToken}${IntegerType.Parse}"
            val grammar = "$fnCall${FunctionCallNode.startToken}${fnCallArguments.joinToString(
                    FunctionCallNode.argumentSeparator)}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(grammar) { _, _ ->
            }
        }
    }
}
