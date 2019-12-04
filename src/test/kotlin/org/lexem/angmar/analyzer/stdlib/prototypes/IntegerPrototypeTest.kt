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

internal class IntegerPrototypeTest {
    @Test
    fun `test toString - without params`() {
        val value = LxmInteger.Num1
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
        val value = LxmInteger.Num1
        val resultValue = value.primitive.toString(radix)

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
            val value = LxmInteger.Num1

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
        val value = LxmInteger.Num1
        val resultValue = value.primitive
        val fnCall = "${PrefixOperatorNode.affirmationOperator}$value"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test negation`() {
        val value = LxmInteger.Num1
        val resultValue = -value.primitive
        val fnCall = "${PrefixOperatorNode.negationOperator}$value"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add`() {
        val value1 = LxmInteger.Num1
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive + value2.primitive
        val fnCall = "$value1${AdditiveExpressionNode.additionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add - float`() {
        val value1 = LxmInteger.Num1
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
        val value1 = LxmInteger.Num1
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
        val value1 = LxmInteger.Num1
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive - value2.primitive
        val fnCall = "$value1${AdditiveExpressionNode.subtractionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test sub - float`() {
        val value1 = LxmInteger.Num1
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
        val value1 = LxmInteger.Num1
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive * value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.multiplicationOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test multiplication - float`() {
        val value1 = LxmInteger.Num1
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
        val value1 = LxmInteger.Num10
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive / value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.divisionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test division - float`() {
        val value1 = LxmInteger.Num1
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
        val value1 = LxmInteger.Num1
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive / value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.integerDivisionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test integer division - float`() {
        val value1 = LxmInteger.Num1
        val value2 = LxmFloat.from(2.5f)
        val resultValue = value1.primitive / value2.primitive.toInt()
        val fnCall = "$value1${MultiplicativeExpressionNode.integerDivisionOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test reminder`() {
        val value1 = LxmInteger.Num1
        val value2 = LxmInteger.Num2
        val resultValue = value1.primitive % value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.reminderOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test reminder - float`() {
        val value1 = LxmInteger.Num10
        val value2 = LxmFloat.from(4.21f)
        val resultValue = value1.primitive % value2.primitive
        val fnCall = "$value1${MultiplicativeExpressionNode.reminderOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmFloat ?: throw Error("The result must be LxmFloat")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }
}
