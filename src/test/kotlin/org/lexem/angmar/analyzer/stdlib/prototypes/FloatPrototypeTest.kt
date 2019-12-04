package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*
import kotlin.math.*

internal class FloatPrototypeTest {
    @Test
    fun `test integerPart`() {
        val value = LxmFloat.from(34.65f)
        val resultValue = 34
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.IntegerPart}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test decimalPart`() {
        val value = LxmFloat.from(34.125f)
        val resultValue = 0.125f
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.DecimalPart}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test toExponential - without params`() {
        val value = LxmFloat.Num0_5
        val resultValue = "5e-1"
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.ToExponential}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 2, 15])
    fun `test toExponential - with params`(radix: Int) {
        val value = LxmFloat.from(14.03125f)
        val resultValue = when (radix) {
            0 -> "1e1"
            2 -> "1.4e1"
            else -> "1.403125e1"
        }
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val args = radix.toString()
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.ToExponential}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 2, 15])
    fun `test toExponential - with params - negative`(radix: Int) {
        val value = LxmFloat.from(-14.03125f)
        val resultValue = when (radix) {
            0 -> "-1e1"
            2 -> "-1.4e1"
            else -> "-1.403125e1"
        }
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val args = radix.toString()
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.ToExponential}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test toExponential with incorrect index`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = LxmFloat.Num0_5

            val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
            val args = LxmLogic.True
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.ToExponential}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test toExponential with index lower than 0`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = LxmFloat.Num0_5

            val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
            val args = LxmInteger.Num_1
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.ToExponential}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 2, 10])
    fun `test toFixed`(radix: Int) {
        val value = LxmFloat.from(14.03125f)
        val resultValue = when (radix) {
            0 -> "14"
            2 -> "14.03"
            else -> "14.0312500000"
        }
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val args = radix.toString()
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.ToFixed}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 2, 10])
    fun `test toFixed - negative`(radix: Int) {
        val value = LxmFloat.from(-14.03125f)
        val resultValue = when (radix) {
            0 -> "-14"
            2 -> "-14.03"
            else -> "-14.0312500000"
        }
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val args = radix.toString()
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.ToFixed}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test toFixed with index lower than 0`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = LxmFloat.Num0_5

            val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
            val args = LxmInteger.Num_1
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${FloatPrototype.ToFixed}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    fun `test toString - without params`() {
        val value = LxmFloat.Num0_5
        val resultValue = value.primitive.toString()
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 8, 10, 16])
    fun `test toString - with radix`(radix: Int) {
        val value = when (radix) {
            2 -> "0b101.1101"
            8 -> "0o1627.362"
            10 -> "0d68.03125"
            else -> "0x34.a5"
        }
        val resultValue = value.substring(2)
        val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
        val args = radix.toString()
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test toString with incorrect index`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val value = LxmFloat.Num0_5

            val valueTxt = "${ParenthesisExpressionNode.startToken}$value${ParenthesisExpressionNode.endToken}"
            val args = LxmLogic.True
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.ToString}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    fun `test affirmation`() {
        val value = LxmFloat.Num0_5
        val resultValue = value.primitive
        val fnCall = "${PrefixOperatorNode.affirmationOperator}$value"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test negation`() {
        val value = LxmFloat.Num0_5
        val resultValue = -value.primitive
        val fnCall = "${PrefixOperatorNode.negationOperator}$value"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive + value2.primitive
        val fnCall = "$value1${AdditiveExpressionNode.additionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add - float`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmFloat.from(0.4f)
        val resultValue = value1.primitive + value2.primitive
        val fnCall = "$value1${AdditiveExpressionNode.additionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add - string`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmString.from("test")
        val resultValue = "${value1.primitive}${value2.primitive}"
        val fnCall =
                "$value1${AdditiveExpressionNode.additionOperator}${StringNode.startToken}$value2${StringNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test sub`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive - value2.primitive
        val fnCall = "$value1${AdditiveExpressionNode.subtractionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test sub - float`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmFloat.from(0.4f)
        val resultValue = value1.primitive - value2.primitive
        val fnCall = "$value1${AdditiveExpressionNode.subtractionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test multiplication`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive * value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.multiplicationOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test multiplication - float`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmFloat.from(0.4f)
        val resultValue = value1.primitive * value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.multiplicationOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test division`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive / value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.divisionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test division - float`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmFloat.from(0.4f)
        val resultValue = value1.primitive / value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.divisionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test integer division`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmInteger.Num2
        val resultValue = truncate(value1.primitive / value2.primitive).toInt()
        val fnCall = "$value1${MultiplicativeExpressionNode.integerDivisionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test integer division - float`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmFloat.from(2.5f)
        val resultValue = truncate(value1.primitive / value2.primitive).toInt()
        val fnCall = "$value1${MultiplicativeExpressionNode.integerDivisionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test reminder`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive % value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.reminderOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test reminder - float`() {
        val value1 = LxmFloat.Num0_5
        val value2 = LxmFloat.from(4.21f)
        val resultValue = value1.primitive % value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.reminderOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }
}
