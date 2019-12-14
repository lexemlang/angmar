package org.lexem.angmar.analyzer.nodes.functional.expressions

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.data.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.utils.*
import java.util.*

internal class AssignOperatorAnalyzerTest {
    @Test
    fun `test assign to nothing`() {
        val varName = "test"
        val right = 2
        val text = "$varName ${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(right, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(right, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test assign replacing`() {
        val varName = "test"
        val left = 6
        val right = 2
        val text = "$varName ${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.from(left))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(right, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(right, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test multiplication`() {
        val varName = "test"
        val left = 6
        val right = 2
        val resultValue = left * right
        val text =
                "$varName ${MultiplicativeExpressionNode.multiplicationOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test division`() {
        val varName = "test"
        val left = 6
        val right = 2
        val resultValue = left / right
        val text =
                "$varName ${MultiplicativeExpressionNode.divisionOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test integer division`() {
        val varName = "test"
        val left = 6
        val right = 2
        val resultValue = left / right
        val text =
                "$varName ${MultiplicativeExpressionNode.integerDivisionOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test integer reminder`() {
        val varName = "test"
        val left = 7
        val right = 4
        val resultValue = left % right
        val text =
                "$varName ${MultiplicativeExpressionNode.reminderOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test addition`() {
        val varName = "test"
        val left = 6
        val right = 2
        val resultValue = left + right
        val text = "$varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test subtraction`() {
        val varName = "test"
        val left = 6
        val right = 2
        val resultValue = left - right
        val text = "$varName ${AdditiveExpressionNode.subtractionOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test left shift`() {
        val varName = "test"
        val leftSize = 3
        val left = BitSet()
        left[1] = true
        val right = 1
        val resultValue = BitSet()
        resultValue[0] = true
        val text = "$varName ${ShiftExpressionNode.leftShiftOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmBitList(BitList(3, left)))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error("The result must be a LxmBitList")
        Assertions.assertEquals(leftSize, result.primitive.size,
                "The size of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, result.primitive.content, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmBitList>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmBitList")

        Assertions.assertEquals(leftSize, property.primitive.size,
                "The size of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, property.primitive.content, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test right shift`() {
        val varName = "test"
        val leftSize = 3
        val left = BitSet()
        left[1] = true
        val right = 1
        val resultValue = BitSet()
        resultValue[2] = true
        val text = "$varName ${ShiftExpressionNode.rightShiftOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmBitList(BitList(3, left)))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error("The result must be a LxmBitList")
        Assertions.assertEquals(leftSize, result.primitive.size,
                "The size of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, result.primitive.content, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmBitList>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmBitList")

        Assertions.assertEquals(leftSize, property.primitive.size,
                "The size of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, property.primitive.content, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test left rotation`() {
        val varName = "test"
        val leftSize = 3
        val left = BitSet()
        left[1] = true
        val right = 1
        val resultSize = leftSize
        val resultValue = BitSet()
        resultValue[0] = true
        val text = "$varName ${ShiftExpressionNode.leftRotationOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmBitList(BitList(3, left)))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error("The result must be a LxmBitList")
        Assertions.assertEquals(resultSize, result.primitive.size,
                "The size of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, result.primitive.content, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmBitList>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmBitList")

        Assertions.assertEquals(resultSize, property.primitive.size,
                "The size of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, property.primitive.content, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test right rotation`() {
        val varName = "test"
        val leftSize = 3
        val left = BitSet()
        left[1] = true
        val right = 1
        val resultSize = leftSize
        val resultValue = BitSet()
        resultValue[2] = true
        val text = "$varName ${ShiftExpressionNode.rightRotationOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmBitList(BitList(3, left)))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmBitList ?: throw Error("The result must be a LxmBitList")
        Assertions.assertEquals(resultSize, result.primitive.size,
                "The size of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, result.primitive.content, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmBitList>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmBitList")

        Assertions.assertEquals(resultSize, property.primitive.size,
                "The size of the value inserted in the stack is incorrect")
        Assertions.assertEquals(resultValue, property.primitive.content, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test logical and`() {
        val varName = "test"
        val left = true
        val right = false
        val resultValue = left.and(right)
        val text = "$varName ${LogicalExpressionNode.andOperator}${AssignOperatorNode.assignOperator} ${LxmLogic.from(
                right)}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmLogic.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmLogic ?: throw Error("The result must be a LxmLogic")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmLogic>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmLogic")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test logical or`() {
        val varName = "test"
        val left = true
        val right = false
        val resultValue = left.or(right)
        val text = "$varName ${LogicalExpressionNode.orOperator}${AssignOperatorNode.assignOperator} ${LxmLogic.from(
                right)}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmLogic.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmLogic ?: throw Error("The result must be a LxmLogic")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmLogic>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmLogic")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test logical xor`() {
        val varName = "test"
        val left = true
        val right = false
        val resultValue = left.xor(right)
        val text = "$varName ${LogicalExpressionNode.xorOperator}${AssignOperatorNode.assignOperator} ${LxmLogic.from(
                right)}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmLogic.from(left))
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmLogic ?: throw Error("The result must be a LxmLogic")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmLogic>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmLogic")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test conditional and`() {
        val varName = "test"
        val left = false
        val right = 1
        val resultValue = left
        val text = "$varName ${ConditionalExpressionNode.andOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmLogic.from(left))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmLogic ?: throw Error("The result must be a LxmLogic")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmLogic>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmLogic")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test conditional or`() {
        val varName = "test"
        val left = LxmLogic.False
        val right = 1
        val resultValue = right
        val text = "$varName ${ConditionalExpressionNode.orOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, left)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(resultValue, result.primitive, "The value inserted in the stack is incorrect")

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property = context.getDereferencedProperty<LxmInteger>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmInteger")

        Assertions.assertEquals(resultValue, property.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test conditional xor`() {
        val varName = "test"
        val left = 2
        val right = 1
        val text = "$varName ${ConditionalExpressionNode.xorOperator}${AssignOperatorNode.assignOperator} $right"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = AssignExpressionNode.Companion::parse)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.from(left))

        TestUtils.processAndCheckEmpty(analyzer)

        analyzer.memory.getLastFromStack() as? LxmNil ?: throw Error("The result must be a LxmNil")
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.getDereferencedProperty<LxmNil>(analyzer.memory, varName) ?: throw Error(
                "The property must be a LxmNil")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }
}
