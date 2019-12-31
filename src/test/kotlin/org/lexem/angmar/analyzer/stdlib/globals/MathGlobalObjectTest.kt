package org.lexem.angmar.analyzer.stdlib.globals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.math.*
import kotlin.streams.*

internal class MathGlobalObjectTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        private val integers = listOf(LxmInteger.from(-12), LxmInteger.Num0, LxmInteger.from(235))
        private val floats = listOf(LxmFloat.from(-16.124f), LxmFloat.from(0.125f), LxmFloat.from(157.23f))

        @JvmStatic
        private fun provideOneParameter(): Stream<Arguments> {
            val result = sequence {
                for (value in integers + floats) {
                    yield(Arguments.of(value))
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideTwoParameters(): Stream<Arguments> {
            val result = sequence {
                for (left in integers + floats) {
                    for (right in integers + floats) {
                        yield(Arguments.of(left, right))
                    }
                }
            }

            return result.asStream()
        }

        @JvmStatic
        private fun provideBetweenOneAndThreeParameters(): Stream<Arguments> {
            val result = sequence {
                yield(Arguments.of(integers[0]))
                yield(Arguments.of(floats[0]))
                yield(Arguments.of(integers[0], floats[0]))
                yield(Arguments.of(floats[0], integers[0]))
                yield(Arguments.of(floats[0], floats[1], floats[2]))
                yield(Arguments.of(integers[0], integers[1], integers[2]))
            }

            return result.asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test abs`(value: LexemPrimitive) {
        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Abs}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            when (value) {
                is LxmInteger -> {
                    val resultValue = abs(value.primitive)
                    result as? LxmInteger ?: throw Error("The result must be LxmInteger")
                    Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
                }
                is LxmFloat -> {
                    val resultValue = abs(value.primitive)
                    result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                    Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
                }
                else -> {
                    Assertions.assertEquals(LxmNil, result, "The result is incorrect")
                }
            }
        }
    }

    @Test
    @Incorrect
    fun `test abs - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Abs}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test acos`(value: LexemPrimitive) {
        val resultValue = acos(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Acos}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test acos - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Acos}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test acosh`(value: LexemPrimitive) {
        val resultValue = acosh(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Acosh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test acosh - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Acosh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test asin`(value: LexemPrimitive) {
        val resultValue = asin(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Asin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test asin - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Asin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test asinh`(value: LexemPrimitive) {
        val resultValue = asinh(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Asinh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test asinh - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Asinh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test atan`(value: LexemPrimitive) {
        val resultValue = atan(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Atan}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test atan - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Atan}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test atanh`(value: LexemPrimitive) {
        val resultValue = atanh(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Atanh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test atanh - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Atanh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideTwoParameters")
    fun `test atan2`(x: LexemPrimitive, y: LexemPrimitive) {
        val xN = if (x is LxmInteger) {
            x.primitive.toDouble()
        } else {
            x as LxmFloat
            x.primitive.toDouble()
        }
        val yN = if (y is LxmInteger) {
            y.primitive.toDouble()
        } else {
            y as LxmFloat
            y.primitive.toDouble()
        }
        val resultValue = atan2(yN, xN).toFloat()

        val args = listOf(y, x).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Atan2}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test atan2 - incorrect type - y`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Atan2}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test atan2 - incorrect type - x`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(2, LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Atan2}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test cbrt`(value: LexemPrimitive) {
        val resultValue = (if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat().pow(1.0f / 3.0f)

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Cbrt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test cbrt - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Cbrt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test ceil`(value: LexemPrimitive) {
        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Ceil}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val resultValue = when (value) {
                is LxmInteger -> value.primitive
                else -> {
                    value as LxmFloat
                    ceil(value.primitive).toInt()
                }
            }
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test ceil - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Ceil}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 4, 10, 1000, -1])
    fun `test clz32`(value: Int) {
        val resultValue = when (value) {
            0 -> 32
            1 -> 31
            4 -> 29
            10 -> 28
            1000 -> 22
            else -> 0
        }

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Clz32}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test clz32 - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Clz32}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test cos`(value: LexemPrimitive) {
        val resultValue = cos(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Cos}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test cos - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Cos}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test cosh`(value: LexemPrimitive) {
        val resultValue = cosh(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Cosh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test cosh - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Cosh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test exp`(value: LexemPrimitive) {
        val resultValue = exp(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Exp}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test exp - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Exp}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test floor`(value: LexemPrimitive) {
        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Floor}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val resultValue = when (value) {
                is LxmInteger -> value.primitive
                else -> {
                    value as LxmFloat
                    floor(value.primitive).toInt()
                }
            }
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test floor - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Floor}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun `test hypot`(count: Int) {
        val argValues = when (count) {
            1 -> listOf(LxmFloat.from(24.6f))
            2 -> listOf(LxmFloat.from(24.6f), LxmInteger.from(24))
            else -> listOf(LxmFloat.from(67f), LxmFloat.from(24.6f), LxmInteger.from(24))
        }
        val resultValue = sqrt(argValues.map {
            if (it is LxmInteger) {
                it.primitive.toFloat()
            } else {
                it as LxmFloat
                it.primitive
            }
        }.map { it * it }.sum())

        val args = argValues.joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Hypot}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test hypot - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Hypot}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test hypot - incorrect number of elements`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Hypot}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test ln`(value: LexemPrimitive) {
        val resultValue = ln(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Ln}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test ln - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Ln}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test log10`(value: LexemPrimitive) {
        val resultValue = log10(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Log10}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test log10 - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Log10}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test log2`(value: LexemPrimitive) {
        val resultValue = log2(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Log2}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test log2 - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Log2}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun `test max`(count: Int) {
        val argValues = when (count) {
            1 -> listOf(LxmFloat.from(24.6f))
            2 -> listOf(LxmFloat.from(24.6f), LxmInteger.from(24))
            else -> listOf(LxmFloat.from(67.9f), LxmFloat.from(24.6f), LxmInteger.from(24))
        }
        val resultValue = (argValues[0] as LxmFloat).primitive

        val args = argValues.joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Max}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test max - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Max}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test max - incorrect number of elements`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Max}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }


    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun `test min`(count: Int) {
        val argValues = when (count) {
            1 -> listOf(LxmFloat.from(24.6f))
            2 -> listOf(LxmFloat.from(5.6f), LxmInteger.from(24))
            else -> listOf(LxmFloat.from(10.49f), LxmFloat.from(24.6f), LxmInteger.from(24))
        }
        val resultValue = (argValues[0] as LxmFloat).primitive

        val args = argValues.joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Min}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test min - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Min}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test min - incorrect number of elements`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Min}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideTwoParameters")
    fun `test pow`(base: LexemPrimitive, exponent: LexemPrimitive) {
        val baseN = if (base is LxmInteger) {
            base.primitive.toFloat()
        } else {
            base as LxmFloat
            base.primitive
        }
        val exponentN = if (exponent is LxmInteger) {
            exponent.primitive.toFloat()
        } else {
            exponent as LxmFloat
            exponent.primitive
        }
        val resultValue = baseN.pow(exponentN)

        val args = listOf(base, exponent).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Pow}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test pow - incorrect type - base`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Pow}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test pow - incorrect type - exponent`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(2, LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Pow}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    fun `test random`() {
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Random}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        val listOfResults = mutableListOf<Float>()
        repeat(20) {
            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
                result as? LxmFloat ?: throw Error("The result must be LxmInteger")

                listOfResults.add(result.primitive)
                Assertions.assertTrue(0 <= result.primitive, "The result is lower than 0")
                Assertions.assertTrue(result.primitive < 1, "The result is greater or equal than 1")
            }
        }

        for ((i, iValue) in listOfResults.withIndex()) {
            for (j in listOfResults.drop(i)) {
                Assertions.assertNotEquals(i, j, "A result is repeated in short time")
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test round`(value: LexemPrimitive) {
        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Round}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val resultValue = when (value) {
                is LxmInteger -> value.primitive
                else -> {
                    value as LxmFloat
                    round(value.primitive).toInt()
                }
            }
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test round - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Round}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }


    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test sign`(value: LexemPrimitive) {
        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Sign}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val resultValue = when (value) {
                is LxmInteger -> sign(value.primitive.toDouble()).toInt()
                else -> {
                    value as LxmFloat
                    sign(value.primitive).toInt()
                }
            }
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test sign - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Sign}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test sin`(value: LexemPrimitive) {
        val resultValue = sin(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Sin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test sin - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Sin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test sinh`(value: LexemPrimitive) {
        val resultValue = sinh(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Sinh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test sinh - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Sinh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test sqrt`(value: LexemPrimitive) {
        val resultValue = sqrt(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Sqrt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            if (!resultValue.isNaN()) {
                result as? LxmFloat ?: throw Error("The result must be LxmFloat")
                Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test sqrt - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Sqrt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test tan`(value: LexemPrimitive) {
        val resultValue = tan(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Tan}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test tan - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Tan}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test tanh`(value: LexemPrimitive) {
        val resultValue = tanh(if (value is LxmInteger) {
            value.primitive.toDouble()
        } else {
            value as LxmFloat
            value.primitive.toDouble()
        }).toFloat()

        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Tanh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test tanh - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Tanh}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideOneParameter")
    fun `test trunc`(value: LexemPrimitive) {
        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Trunc}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val resultValue = when (value) {
                is LxmInteger -> value.primitive
                else -> {
                    value as LxmFloat
                    truncate(value.primitive).toInt()
                }
            }
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test trunc - incorrect type - number`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "${MathGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${MathGlobalObject.Trunc}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }
}
