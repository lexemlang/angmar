package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.utils.*
import java.util.*

internal class BitListPrototypeTest {
    @Test
    fun `test bitwise negation`() {
        val setLength = 8
        val set = BitSet().apply {
            set(1)
            set(2)
            set(4)
            set(6)
        }
        val value = LxmBitList(BitList(setLength, set))
        val resultValue = set.clone() as BitSet
        resultValue.flip(0, 8)
        val fnCall = "${PrefixOperatorNode.bitwiseNegationOperator}$value"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(setLength, resultValue), result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test and - logic`() {
        val setLength = 8
        val set = BitSet().apply {
            set(1)
            set(2)
            set(4)
            set(6)
        }
        val value1 = LxmBitList(BitList(setLength, set))
        val value2 = LxmLogic.False
        val resultValue = BitSet()
        val fnCall = "$value1${LogicalExpressionNode.andOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(setLength, resultValue), result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test and - bitlist`() {
        val setLength1 = 8
        val set1 = BitSet().apply {
            set(1)
            set(2)
            set(4)
            set(6)
        }
        val setLength2 = 3
        val set2 = BitSet().apply {
            set(1)
            set(3)
        }
        val value1 = LxmBitList(BitList(setLength1, set1))
        val value2 = LxmBitList(BitList(setLength2, set2))
        val resultValue = set1.clone() as BitSet
        resultValue.and(set2)
        val fnCall = "$value1${LogicalExpressionNode.andOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(maxOf(setLength1, setLength2), resultValue), result.primitive,
                    "The result is incorrect")
        }
    }

    @Test
    fun `test or - logic`() {
        val setLength = 8
        val set = BitSet().apply {
            set(1)
            set(2)
            set(4)
            set(6)
        }
        val value1 = LxmBitList(BitList(setLength, set))
        val value2 = LxmLogic.True
        val resultValue = BitSet().apply {
            set(0, 8)
        }
        val fnCall = "$value1${LogicalExpressionNode.orOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(setLength, resultValue), result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test or - bitlist`() {
        val setLength1 = 8
        val set1 = BitSet().apply {
            set(1)
            set(2)
            set(4)
            set(6)
        }
        val setLength2 = 3
        val set2 = BitSet().apply {
            set(0)
            set(1)
        }
        val value1 = LxmBitList(BitList(setLength1, set1))
        val value2 = LxmBitList(BitList(setLength2, set2))
        val resultValue = set1.clone() as BitSet
        resultValue.or(set2)
        val fnCall = "$value1${LogicalExpressionNode.orOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(maxOf(setLength1, setLength2), resultValue), result.primitive,
                    "The result is incorrect")
        }
    }

    @Test
    fun `test xor - logic`() {
        val setLength = 8
        val set = BitSet().apply {
            set(1)
            set(2)
            set(4)
            set(6)
        }
        val value1 = LxmBitList(BitList(setLength, set))
        val value2 = LxmLogic.True
        val resultValue = set.clone() as BitSet
        resultValue.flip(0, 8)
        val fnCall = "$value1${LogicalExpressionNode.xorOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(setLength, resultValue), result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test xor - bitlist`() {
        val setLength1 = 8
        val set1 = BitSet().apply {
            set(1)
            set(2)
            set(4)
            set(6)
        }
        val setLength2 = 3
        val set2 = BitSet().apply {
            set(0)
            set(1)
        }
        val value1 = LxmBitList(BitList(setLength1, set1))
        val value2 = LxmBitList(BitList(setLength2, set2))
        val resultValue = set1.clone() as BitSet
        resultValue.xor(set2)
        val fnCall = "$value1${LogicalExpressionNode.xorOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(maxOf(setLength1, setLength2), resultValue), result.primitive,
                    "The result is incorrect")
        }
    }

    @Test
    fun `test left shift`() {
        val setLength = 6
        val set = BitSet().apply {
            set(0)
            set(3)
            set(4)
            set(5)
        }
        val value1 = LxmBitList(BitList(setLength, set))
        val value2 = LxmInteger.Num2
        val resultValue = BitSet().apply {
            set(1)
            set(2)
            set(3)
        }
        val fnCall = "$value1${ShiftExpressionNode.leftShiftOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(setLength, resultValue), result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test left shift - incorrect type`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val setLength = 6
            val set = BitSet().apply {
                set(0)
                set(3)
                set(4)
                set(5)
            }
            val value1 = LxmBitList(BitList(setLength, set))
            val value2 = LxmLogic.True
            val fnCall = "$value1${ShiftExpressionNode.leftShiftOperator}$value2"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    fun `test right shift`() {
        val setLength = 6
        val set = BitSet().apply {
            set(0)
            set(3)
            set(4)
            set(5)
        }
        val value1 = LxmBitList(BitList(setLength, set))
        val value2 = LxmInteger.Num2
        val resultValue = BitSet().apply {
            set(2)
            set(5)
        }
        val fnCall = "$value1${ShiftExpressionNode.rightShiftOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(setLength, resultValue), result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test right shift - incorrect type`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val setLength = 6
            val set = BitSet().apply {
                set(0)
                set(3)
                set(4)
                set(5)
            }
            val value1 = LxmBitList(BitList(setLength, set))
            val value2 = LxmLogic.True
            val fnCall = "$value1${ShiftExpressionNode.rightShiftOperator}$value2"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    fun `test left rotation`() {
        val setLength = 6
        val set = BitSet().apply {
            set(0)
            set(3)
            set(4)
            set(5)
        }
        val value1 = LxmBitList(BitList(setLength, set))
        val value2 = LxmInteger.Num2
        val resultValue = BitSet().apply {
            set(1)
            set(2)
            set(3)
            set(4)
        }
        val fnCall = "$value1${ShiftExpressionNode.leftRotationOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(setLength, resultValue), result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test left rotation - incorrect type`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val setLength = 6
            val set = BitSet().apply {
                set(0)
                set(3)
                set(4)
                set(5)
            }
            val value1 = LxmBitList(BitList(setLength, set))
            val value2 = LxmLogic.True
            val fnCall = "$value1${ShiftExpressionNode.leftRotationOperator}$value2"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    fun `test right rotation`() {
        val setLength = 6
        val set = BitSet().apply {
            set(0)
            set(3)
            set(4)
            set(5)
        }
        val value1 = LxmBitList(BitList(setLength, set))
        val value2 = LxmInteger.Num2
        val resultValue = BitSet().apply {
            set(2)
            set(5)
            set(0)
            set(1)
        }
        val fnCall = "$value1${ShiftExpressionNode.rightRotationOperator}$value2"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmBitList ?: throw Error("The result must be LxmBitList")
            Assertions.assertEquals(BitList(setLength, resultValue), result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test right rotation - incorrect type`() {
        TestUtils.assertCompilerException(AngmarCompilerExceptionType.IncompatibleType) {
            val setLength = 6
            val set = BitSet().apply {
                set(0)
                set(3)
                set(4)
                set(5)
            }
            val value1 = LxmBitList(BitList(setLength, set))
            val value2 = LxmLogic.True
            val fnCall = "$value1${ShiftExpressionNode.rightRotationOperator}$value2"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }
}
