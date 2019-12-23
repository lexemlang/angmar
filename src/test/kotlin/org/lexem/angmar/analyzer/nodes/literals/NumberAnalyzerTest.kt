package org.lexem.angmar.analyzer.nodes.literals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import java.lang.Math.*
import java.util.stream.*
import kotlin.streams.*

internal class NumberAnalyzerTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        private const val integerPart = 27600
        private const val integerForFloat = 275

        @JvmStatic
        private fun provideBinaryIntegers() = generateIntegers(2)

        @JvmStatic
        private fun provideOctalIntegers() = generateIntegers(8)

        @JvmStatic
        private fun provideDecimalIntegers() = generateIntegers(10)

        @JvmStatic
        private fun provideHexadecimalIntegers() = generateIntegers(16)

        @JvmStatic
        private fun provideBinaryFloats() = generateFloat(2)

        @JvmStatic
        private fun provideOctalFloats() = generateFloat(8)

        @JvmStatic
        private fun provideDecimalFloats() = generateFloat(10)

        @JvmStatic
        private fun provideHexadecimalFloats() = generateFloat(16)


        // AUX METHODS --------------------------------------------------------

        private fun generateIntegers(radix: Int): Stream<Arguments> {
            val result = sequence {
                for (withExponent in 0..3) {
                    for (withDecimals in listOf(false, true)) {
                        var num = integerPart.toString(radix)

                        if (withDecimals) {
                            num += "${NumberNode.decimalSeparator}0"
                        }

                        val numAsInt = when (withExponent) {
                            1 -> {
                                num += "${NumberNode.hexadecimalExponentSeparator[0]}1"
                                (integerPart * pow(radix.toDouble(), 1.0)).toInt()
                            }
                            2 -> {
                                num += "${NumberNode.hexadecimalExponentSeparator[0]}${NumberNode.exponentPositiveSign}1"
                                (integerPart * pow(radix.toDouble(), 1.0)).toInt()
                            }
                            3 -> {
                                num += "${NumberNode.hexadecimalExponentSeparator[0]}${NumberNode.exponentNegativeSign}1"
                                (integerPart * pow(radix.toDouble(), -1.0)).toInt()
                            }
                            else -> integerPart
                        }

                        yield(Arguments.of(num, numAsInt))
                    }
                }
            }

            return result.asStream()
        }

        private fun generateFloat(radix: Int): Stream<Arguments> {
            val result = sequence {
                for (withExponent in 0..3) {
                    var num = integerForFloat.toString(radix) + "${NumberNode.decimalSeparator}101"
                    var numAsFloat = integerForFloat.toFloat() + 1.0f / radix + 1.0f / (radix * radix * radix)

                    numAsFloat = when (withExponent) {
                        1 -> {
                            num += "${NumberNode.hexadecimalExponentSeparator[0]}1"
                            (numAsFloat * pow(radix.toDouble(), 1.0)).toFloat()
                        }
                        2 -> {
                            num += "${NumberNode.hexadecimalExponentSeparator[0]}${NumberNode.exponentPositiveSign}1"
                            (numAsFloat * pow(radix.toDouble(), 1.0)).toFloat()
                        }
                        3 -> {
                            num += "${NumberNode.hexadecimalExponentSeparator[0]}${NumberNode.exponentNegativeSign}1"
                            (numAsFloat * pow(radix.toDouble(), -1.0)).toFloat()
                        }
                        else -> numAsFloat
                    }

                    yield(Arguments.of(num, numAsFloat))
                }
            }

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideBinaryIntegers")
    fun `test binary integers`(number: String, numberAsInt: Int) {
        val text = "${NumberNode.binaryPrefix}$number"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(numberAsInt, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideOctalIntegers")
    fun `test octal integers`(number: String, numberAsInt: Int) {
        val text = "${NumberNode.octalPrefix}$number"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(numberAsInt, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideDecimalIntegers")
    fun `test decimal integers`(number: String, numberAsInt: Int) {
        val text = "${NumberNode.decimalPrefix}$number"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(numberAsInt, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideDecimalIntegers")
    fun `test decimal integers without prefix`(number: String, numberAsInt: Int) {
        val analyzer = TestUtils.createAnalyzerFrom(number,
                parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(numberAsInt, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideHexadecimalIntegers")
    fun `test hexadecimal integers`(number: String, numberAsInt: Int) {
        val text = "${NumberNode.hexadecimalPrefix}$number"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error(
                "The value of the stack must be a LxmInteger")
        Assertions.assertEquals(numberAsInt, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    // FLOATS ---

    @ParameterizedTest
    @MethodSource("provideBinaryFloats")
    fun `test binary floats`(number: String, numberAsFloat: Float) {
        val text = "${NumberNode.binaryPrefix}$number"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmFloat ?: throw Error(
                "The value of the stack must be a LxmFloat")
        Assertions.assertEquals(numberAsFloat, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideOctalFloats")
    fun `test octal floats`(number: String, numberAsFloat: Float) {
        val text = "${NumberNode.octalPrefix}$number"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmFloat ?: throw Error(
                "The value of the stack must be a LxmFloat")
        Assertions.assertEquals(numberAsFloat, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideDecimalFloats")
    fun `test decimal floats`(number: String, numberAsFloat: Float) {
        val text = "${NumberNode.decimalPrefix}$number"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmFloat ?: throw Error(
                "The value of the stack must be a LxmFloat")
        Assertions.assertEquals(numberAsFloat, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideDecimalFloats")
    fun `test decimal floats without prefix`(number: String, numberAsFloat: Float) {
        val analyzer = TestUtils.createAnalyzerFrom(number,
                parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmFloat ?: throw Error(
                "The value of the stack must be a LxmFloat")
        Assertions.assertEquals(numberAsFloat, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @MethodSource("provideHexadecimalFloats")
    fun `test hexadecimal floats`(number: String, numberAsFloat: Float) {
        val text = "${NumberNode.hexadecimalPrefix}$number"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = NumberNode.Companion::parseAnyNumberDefaultDecimal)
        TestUtils.processAndCheckEmpty(analyzer)

        val stackValue = analyzer.memory.getLastFromStack() as? LxmFloat ?: throw Error(
                "The value of the stack must be a LxmFloat")
        Assertions.assertEquals(numberAsFloat, stackValue.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
