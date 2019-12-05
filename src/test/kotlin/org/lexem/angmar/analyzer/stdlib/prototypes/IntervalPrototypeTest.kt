package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.data.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class IntervalPrototypeTest {
    @Test
    fun `test isEmpty`() {
        val interval = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val valueTxt = "${ParenthesisExpressionNode.startToken}$interval${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${IntervalPrototype.IsEmpty}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).isEmpty

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmLogic ?: throw Error("The result must be LxmLogic")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test pointCount`() {
        val interval = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val valueTxt = "${ParenthesisExpressionNode.startToken}$interval${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${IntervalPrototype.PointCount}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).pointCount.toInt()

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test unicodeNot`() {
        val interval = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val valueTxt = "${ParenthesisExpressionNode.startToken}$interval${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${IntervalPrototype.UnicodeNot}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).unicodeNot()

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test not`() {
        val interval = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val fnCall = "${PrefixOperatorNode.notOperator}$interval"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).not()

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test and - integer`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right = 2
        val fnCall = "$left ${LogicalExpressionNode.andOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).common(right)

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test and - interval`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right =
                "${IntervalNode.macroName}${IntervalNode.startToken}7${IntervalElementNode.rangeToken}29${IntervalNode.endToken}"
        val fnCall = "$left ${LogicalExpressionNode.andOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).common(IntegerRange.new(7, 29))

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test or - integer`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right = 4
        val fnCall = "$left ${LogicalExpressionNode.orOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).plus(right)

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test or - interval`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right =
                "${IntervalNode.macroName}${IntervalNode.startToken}7${IntervalElementNode.rangeToken}29${IntervalNode.endToken}"
        val fnCall = "$left ${LogicalExpressionNode.orOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).plus(IntegerRange.new(7, 29))

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test xor - integer`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right = 3
        val fnCall = "$left ${LogicalExpressionNode.xorOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).notCommon(10).notCommon(right)

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test xor - interval`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right =
                "${IntervalNode.macroName}${IntervalNode.startToken}7${IntervalElementNode.rangeToken}29${IntervalNode.endToken}"
        val fnCall = "$left ${LogicalExpressionNode.xorOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).notCommon(IntegerRange.new(7, 29))

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add - integer`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right = 4
        val fnCall = "$left ${AdditiveExpressionNode.additionOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).plus(right)

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test add - interval`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right =
                "${IntervalNode.macroName}${IntervalNode.startToken}7${IntervalElementNode.rangeToken}29${IntervalNode.endToken}"
        val fnCall = "$left ${AdditiveExpressionNode.additionOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).plus(IntegerRange.new(7, 29))

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test sub - integer`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right = 3
        val fnCall = "$left ${AdditiveExpressionNode.subtractionOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).minus(right)

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test sub - interval`() {
        val left = "${IntervalNode.macroName}${IntervalNode.startToken}2 3 10${IntervalNode.endToken}"
        val right =
                "${IntervalNode.macroName}${IntervalNode.startToken}7${IntervalElementNode.rangeToken}29${IntervalNode.endToken}"
        val fnCall = "$left ${AdditiveExpressionNode.subtractionOperator} $right"
        val resultValue = IntegerInterval.new(IntegerRange.new(2, 3)).plus(10).minus(IntegerRange.new(7, 29))

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInterval ?: throw Error("The result must be LxmInterval")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }
}
