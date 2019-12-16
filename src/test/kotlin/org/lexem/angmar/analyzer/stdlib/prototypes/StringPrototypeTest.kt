package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class StringPrototypeTest {
    @Test
    fun `test length`() {
        val value = "test"
        val resultValue = value.length

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Length}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test charsAt`() {
        val value = "test"
        val resultValue = "eteste"

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(1,
                "${IntervalNode.macroName}${IntervalNode.startToken}0${IntervalElementNode.rangeToken}4${IntervalNode.endToken}",
                20, 1).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.CharsAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test charsAt - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.CharsAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test unicodePointsAt`() {
        val value = "test"
        val resultValue = listOf('e'.toInt(), 't'.toInt(), 'e'.toInt(), 's'.toInt(), 't'.toInt(), 'e'.toInt())

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(1,
                "${IntervalNode.macroName}${IntervalNode.startToken}0${IntervalElementNode.rangeToken}4${IntervalNode.endToken}",
                20, 1).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.UnicodePointsAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val list = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be LxmList")
            val listValues = list.getAllCells()
            Assertions.assertEquals(resultValue.size, listValues.size, "The result is incorrect")

            for ((primitive, res) in listValues.zip(resultValue)) {
                primitive as? LxmInteger ?: throw Error("The result must be a LxmList of LxmIntegers")

                Assertions.assertEquals(res, primitive.primitive, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test unicodePointsAt - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.UnicodePointsAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test endsWithAny`(isOk: Boolean) {
        val value = "test"
        val resultValue = LxmLogic.from(isOk)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("x", if (isOk) {
            "t"
        } else {
            "y"
        }).joinToString(FunctionCallNode.argumentSeparator) { "${StringNode.startToken}$it${StringNode.endToken}" }
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.EndsWithAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test endsWithAny - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.EndsWithAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test startsWithAny`(isOk: Boolean) {
        val value = "test"
        val resultValue = LxmLogic.from(isOk)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("x", if (isOk) {
            "t"
        } else {
            "y"
        }).joinToString(FunctionCallNode.argumentSeparator) { "${StringNode.startToken}$it${StringNode.endToken}" }
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.StartsWithAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test startsWithAny - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.StartsWithAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test containsAny`(isOk: Boolean) {
        val value = "test"
        val resultValue = LxmLogic.from(isOk)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("x", if (isOk) {
            "es"
        } else {
            "y"
        }).joinToString(FunctionCallNode.argumentSeparator) { "${StringNode.startToken}$it${StringNode.endToken}" }
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.ContainsAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test containsAny - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.ContainsAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test containsAll`(isOk: Boolean) {
        val value = "test"
        val resultValue = LxmLogic.from(isOk)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("t", if (isOk) {
            "es"
        } else {
            "y"
        }).joinToString(FunctionCallNode.argumentSeparator) { "${StringNode.startToken}$it${StringNode.endToken}" }
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.ContainsAll}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test containsAll - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.ContainsAll}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test indexOf`(isOk: Boolean) {
        val value = "testestestest"
        val resultValue = if (isOk) {
            LxmInteger.Num1
        } else {
            LxmNil
        }

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(if (isOk) {
            "es"
        } else {
            "y"
        }).joinToString(FunctionCallNode.argumentSeparator) { "${StringNode.startToken}$it${StringNode.endToken}" }
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.IndexOf}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test indexOf - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.IndexOf}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test lastIndexOf`(isOk: Boolean) {
        val value = "testestestest"
        val resultValue = if (isOk) {
            LxmInteger.Num10
        } else {
            LxmNil
        }

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(if (isOk) {
            "es"
        } else {
            "y"
        }).joinToString(FunctionCallNode.argumentSeparator) { "${StringNode.startToken}$it${StringNode.endToken}" }
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.LastIndexOf}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test lastIndexOf - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.LastIndexOf}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test padStart - without padString`() {
        val value = "test"
        val length = 20
        val resultValue = "test".padStart(length)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(length).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.PadStart}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test padStart - with padString`() {
        val value = "test"
        val padString = "12345"
        val resultValue = padString + padString.substring(0, 3) + "test"
        val length = resultValue.length

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(length, "${StringNode.startToken}$padString${StringNode.endToken}").joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.PadStart}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test padStart - incorrect type - length`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.PadStart}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test padStart - incorrect type - padString`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(1, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.PadStart}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test padEnd - without padString`() {
        val value = "test"
        val length = 20
        val resultValue = "test".padEnd(length)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(length).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.PadEnd}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test padEnd - with padString`() {
        val value = "test"
        val padString = "12345"
        val resultValue = "test" + padString + padString.substring(0, 3)
        val length = resultValue.length

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(length, "${StringNode.startToken}$padString${StringNode.endToken}").joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.PadEnd}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test padEnd - incorrect type - length`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.PadEnd}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test padEnd - incorrect type - padString`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(1, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.PadEnd}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test repeat - without separator`() {
        val value = "test"
        val count = 5
        val resultValue = List(count) { value }.joinToString("")

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Repeat}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test repeat - with separator`() {
        val value = "test"
        val count = 5
        val separator = " - "
        val resultValue = List(count) { value }.joinToString(separator)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(count, "${StringNode.startToken}$separator${StringNode.endToken}").joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Repeat}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test repeat - incorrect type - count`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Repeat}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test repeat - incorrect type - separator`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(1, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Repeat}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test replace - without insensible`() {
        val value = "test"
        val original = "e"
        val replace = "a"
        val resultValue = value.replace(original, replace)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("${StringNode.startToken}$original${StringNode.endToken}",
                "${StringNode.startToken}$replace${StringNode.endToken}").joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test replace - with insensible`() {
        val value = "tEsT"
        val original = "e"
        val replace = "a"
        val resultValue = value.replace(original, replace, ignoreCase = true)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("${StringNode.startToken}$original${StringNode.endToken}",
                "${StringNode.startToken}$replace${StringNode.endToken}", LxmLogic.True).joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test replace - incorrect type - original`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test replace - incorrect type - replace`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf("${StringNode.startToken}${StringNode.endToken}", LxmLogic.False).joinToString(
                    FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test replace - incorrect type - insensible`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf("${StringNode.startToken}${StringNode.endToken}",
                    "${StringNode.startToken}${StringNode.endToken}", LxmInteger.Num0).joinToString(
                    FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test slice - without count`() {
        val value = "test"
        val index = 1
        val resultValue = value.substring(index)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(index).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test slice - with count`() {
        val value = "test"
        val index = 1
        val count = 2
        val resultValue = value.substring(index, count + index)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(index, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test slice - incorrect type - from`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test slice - incorrect type - count`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(1, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test split`(isOk: Boolean) {
        val value = "test - test - test"
        val separator = if (isOk) {
            "-"
        } else {
            "y"
        }
        val resultValue = value.split(separator)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf(separator).joinToString(
                FunctionCallNode.argumentSeparator) { "${StringNode.startToken}$it${StringNode.endToken}" }
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Split}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val list = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be LxmList")
            val listValues = list.getAllCells()
            Assertions.assertEquals(resultValue.size, listValues.size, "The result is incorrect")

            for ((primitive, res) in listValues.zip(resultValue)) {
                primitive as? LxmString ?: throw Error("The result must be a LxmList of LxmString")

                Assertions.assertEquals(res, primitive.primitive, "The result is incorrect")
            }
        }
    }

    @Test
    @Incorrect
    fun `test split - incorrect type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = "test"
            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Split}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test toLowerCase`() {
        val value = "tEsT"
        val resultValue = value.toUnicodeLowercase()

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.ToLowercase}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test toUpperCase`() {
        val value = "tEsT"
        val resultValue = value.toUnicodeUppercase()

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.ToUppercase}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test trimEnd`() {
        val value = "   test   "
        val resultValue = value.trimEnd()

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.TrimEnd}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test trimStart`() {
        val value = "   test   "
        val resultValue = value.trimStart()

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.TrimStart}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test trim`() {
        val value = "   test   "
        val resultValue = value.trim()

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${StringNode.startToken}$value${StringNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${StringPrototype.Trim}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add - int`() {
        val value1 = "value"
        val value2 = LxmInteger.Num2
        val resultValue = "$value1${value2.primitive}"
        val fnCall =
                "${StringNode.startToken}$value1${StringNode.endToken}${AdditiveExpressionNode.additionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add - string`() {
        val value1 = "value"
        val value2 = LxmString.from("test")
        val resultValue = "$value1${value2.primitive}"
        val fnCall =
                "${StringNode.startToken}$value1${StringNode.endToken}${AdditiveExpressionNode.additionOperator}${StringNode.startToken}$value2${StringNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }
}
