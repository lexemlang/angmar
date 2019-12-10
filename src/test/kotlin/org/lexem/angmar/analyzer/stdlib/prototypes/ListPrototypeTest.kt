package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ListPrototypeTest {
    @Test
    fun `test size`() {
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)
        val resultValue = list.size

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Size}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test freeze`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)
        val resultValue = LxmNil

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Freeze}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val list = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")

            Assertions.assertTrue(list.isImmutable, "The isImmutable property is incorrect")
        }
    }

    @Test
    fun `test isFrozen`() {
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)
        val resultValue = LxmLogic.True

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ListNode.constantToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.IsFrozen}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test every`(isOk: Boolean) {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num10, LxmInteger.Num2, LxmInteger.Num0)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val value2Check = if (isOk) {
            0
        } else {
            2
        }

        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${RelationalExpressionNode.greaterOrEqualThanOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Every}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test every - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Every}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test filter`() {
        val variable = "testVariable"
        val list = listOf(0, 1, 2, 3, 4)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${RelationalExpressionNode.greaterThanOperator} 2"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Filter}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resultList = list.filter { it > 2 }
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(resultList.size, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in resultList.zip(resList.getAllCells())) {
                resElement as? LxmInteger ?: throw Error("All elements in the result list must be LxmInteger")
                Assertions.assertEquals(listElement, resElement.primitive, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                oriElement as? LxmInteger ?: throw Error("All elements in the original list must be LxmInteger")
                Assertions.assertEquals(listElement, oriElement.primitive, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test filter - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Filter}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test forEach`() {
        val variable = "testVariable"
        val variableAccumulator = "testVariableAccumulator"
        val list = listOf(1, 2, 3, 4, 5)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "$variableAccumulator ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $checkFunctionArg"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.ForEach}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil, variableAccumulator to LxmInteger.Num0)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                oriElement as? LxmInteger ?: throw Error("All elements in the original list must be LxmInteger")
                Assertions.assertEquals(listElement, oriElement.primitive, "The original list has not been modified")
            }

            val accumulator =
                    context.getDereferencedProperty<LxmInteger>(analyzer.memory, variableAccumulator) ?: throw Error(
                            "The variable must contain a LxmInteger")
            Assertions.assertEquals(list.sum(), accumulator.primitive, "The variable is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test forEach - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.ForEach}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test find`(isOk: Boolean) {
        val variable = "testVariable"
        val list = listOf(LxmFloat.Num0_5, LxmInteger.Num1, LxmNil, LxmInteger.Num1, LxmLogic.True)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val value2Check = if (isOk) {
            LxmInteger.Num1
        } else {
            LxmLogic.False
        }

        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${RelationalExpressionNode.identityOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Find}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            if (isOk) {
                val obj = result?.dereference(analyzer.memory) as? LxmObject ?: throw Error(
                        "The result must be a LxmObject")
                val index = obj.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Index)
                val value = obj.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)

                Assertions.assertEquals(LxmInteger.Num1, index, "The index property is incorrect")
                Assertions.assertEquals(LxmInteger.Num1, value, "The value property is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test find - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Find}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test findLast`(isOk: Boolean) {
        val variable = "testVariable"
        val list = listOf(LxmFloat.Num0_5, LxmInteger.Num1, LxmNil, LxmInteger.Num1, LxmLogic.True)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val value2Check = if (isOk) {
            LxmInteger.Num1
        } else {
            LxmLogic.False
        }

        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${RelationalExpressionNode.identityOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.FindLast}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            if (isOk) {
                val obj = result?.dereference(analyzer.memory) as? LxmObject ?: throw Error(
                        "The result must be a LxmObject")
                val index = obj.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Index) as? LxmInteger
                        ?: throw Error("The index property must be a LxmInteger")
                val value = obj.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)

                Assertions.assertEquals(3, index.primitive, "The index property is incorrect")
                Assertions.assertEquals(LxmInteger.Num1, value, "The value property is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test findLast - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.FindLast}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test indexOf`(isOk: Boolean) {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num1)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val valueIndex = 0
        val value = if (isOk) {
            list[valueIndex]
        } else {
            LxmFloat.Num_1
        }

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.IndexOf}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            if (isOk) {
                result as? LxmInteger ?: throw Error("The result must be LxmInteger")
                Assertions.assertEquals(valueIndex, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test lastIndexOf`(isOk: Boolean) {
        val variable = "testVariable"
        val list =
                listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmFloat.Num0_5, LxmNil, LxmLogic.True,
                        LxmFloat.Num0_5, LxmNil, LxmNil, LxmInteger.Num1)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val valueIndex = 10
        val value = if (isOk) {
            list[valueIndex]
        } else {
            LxmFloat.Num_1
        }

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(value).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.LastIndexOf}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            if (isOk) {
                result as? LxmInteger ?: throw Error("The result must be LxmInteger")
                Assertions.assertEquals(valueIndex, result.primitive, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test containsAny - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.ContainsAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test containsAny - with values`(isOk: Boolean) {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val additionElements = listOf(LxmFloat.Num_1, if (isOk) {
            LxmLogic.True
        } else {
            LxmInteger.Num2
        })

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.ContainsAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test containsAll - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.ContainsAll}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test containsAll - with values`(isOk: Boolean) {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val additionElements = listOf(LxmFloat.Num0_5, if (isOk) {
            LxmLogic.True
        } else {
            LxmInteger.Num2
        })

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.ContainsAll}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test map`() {
        val variable = "testVariable"
        val list = listOf(0, 1, 2, 3, 4)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${AdditiveExpressionNode.additionOperator} 1"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Map}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(list.size, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in list.zip(resList.getAllCells())) {
                resElement as? LxmInteger ?: throw Error("All elements in the result list must be LxmInteger")
                Assertions.assertEquals(listElement + 1, resElement.primitive, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                oriElement as? LxmInteger ?: throw Error("All elements in the original list must be LxmInteger")
                Assertions.assertEquals(listElement, oriElement.primitive, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test map - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Map}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test reduce`() {
        val variable = "testVariable"
        val list = listOf(1, 2, 3, 4, 5)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val checkFunctionArg1 = "acc"
        val checkFunctionArg2 = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg1 ${AdditiveExpressionNode.additionOperator} $checkFunctionArg2"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg1${FunctionParameterListNode.parameterSeparator}$checkFunctionArg2${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(0, checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Reduce}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be a LxmInteger")
            Assertions.assertEquals(list.sum(), result.primitive, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                oriElement as? LxmInteger ?: throw Error("All elements in the original list must be LxmInteger")
                Assertions.assertEquals(listElement, oriElement.primitive, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test reduce - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(2, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Reduce}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test any`(isOk: Boolean) {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val value2Check = if (isOk) {
            LxmLogic.True
        } else {
            LxmInteger.Num2
        }

        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${RelationalExpressionNode.identityOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Any}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test any - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Any}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test remove - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Remove}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test remove - with values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10, LxmLogic.True)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val additionElements = listOf(LxmLogic.True)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Remove}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val resultList = list.filter { it != LxmLogic.True }
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(resultList.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in resultList.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test removeAt - without count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.RemoveAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size - 1, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.take(at).zip(oriList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(at + 1).zip(oriList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test removeAt - with count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 2
        val count = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.RemoveAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size - count, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.take(at).zip(oriList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(at + count).zip(oriList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test removeAt - with count - at exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 20
        val count = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.RemoveAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test removeAt - with count - count exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 2
        val count = 20

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.RemoveAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(at, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test removeAt - incorrect at - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.RemoveAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test removeAt - incorrect at - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(-1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.RemoveAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test removeAt - incorrect count - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(2, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.RemoveAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test removeAt - incorrect count - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(2, -1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.RemoveAt}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test joinToString`() {
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)
        val resultValue = list.joinToString("")

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.JoinToString}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test joinToString - with separator`() {
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)
        val separator = " --- "
        val resultValue = list.joinToString(separator)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("${StringNode.startToken}$separator${StringNode.endToken}").joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.JoinToString}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmString ?: throw Error("The result must be LxmString")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test joinToString - incorrect separator - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.JoinToString}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test copyWithin - without count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val targetVariable = "targetTestVariable"
        val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
        val targetTxt = target.joinToString(ListNode.elementSeparator)

        val at = 2
        val from = 1

        val preFunctionCall =
                "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(targetVariable, at, from).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }

            val targetList = context.getDereferencedProperty<LxmList>(analyzer.memory, targetVariable) ?: throw Error(
                    "The target variable must contain a LxmList")
            Assertions.assertEquals(at + list.size - from, targetList.actualListSize, "The target list is incorrect")

            for ((listElement, oriElement) in target.take(at).zip(targetList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(from).zip(targetList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }
        }
    }

    @Test
    fun `test copyWithin - with count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val targetVariable = "targetTestVariable"
        val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
        val targetTxt = target.joinToString(ListNode.elementSeparator)

        val at = 2
        val from = 1
        val count = 3

        val preFunctionCall =
                "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(targetVariable, at, from, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }

            val targetList = context.getDereferencedProperty<LxmList>(analyzer.memory, targetVariable) ?: throw Error(
                    "The target variable must contain a LxmList")
            Assertions.assertEquals(at + count, targetList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in target.take(at).zip(targetList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(from).take(count).zip(targetList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }
        }
    }

    @Test
    fun `test copyWithin - with count - at exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val targetVariable = "targetTestVariable"
        val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
        val targetTxt = target.joinToString(ListNode.elementSeparator)

        val at = 20
        val from = 1
        val count = 3

        val preFunctionCall =
                "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(targetVariable, at, from, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }

            val targetList = context.getDereferencedProperty<LxmList>(analyzer.memory, targetVariable) ?: throw Error(
                    "The target variable must contain a LxmList")
            Assertions.assertEquals(target.size + count, targetList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in target.zip(targetList.getAllCells().take(target.size))) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(from).take(count).zip(
                    targetList.getAllCells().drop(target.size))) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }
        }
    }

    @Test
    fun `test copyWithin - with count - from exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val targetVariable = "targetTestVariable"
        val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
        val targetTxt = target.joinToString(ListNode.elementSeparator)

        val at = 2
        val from = 20
        val count = 3

        val preFunctionCall =
                "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(targetVariable, at, from, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }

            val targetList = context.getDereferencedProperty<LxmList>(analyzer.memory, targetVariable) ?: throw Error(
                    "The target variable must contain a LxmList")
            Assertions.assertEquals(target.size, targetList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in target.zip(targetList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }
        }
    }

    @Test
    fun `test copyWithin - with count - count exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val targetVariable = "targetTestVariable"
        val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
        val targetTxt = target.joinToString(ListNode.elementSeparator)

        val at = 2
        val from = 1
        val count = 50

        val preFunctionCall =
                "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(targetVariable, at, from, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }

            val targetList = context.getDereferencedProperty<LxmList>(analyzer.memory, targetVariable) ?: throw Error(
                    "The target variable must contain a LxmList")
            Assertions.assertEquals(at + list.size - from, targetList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in target.take(at).zip(targetList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(from).zip(targetList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The target list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test copyWithin - incorrect target - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val variable = "testVariable"
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val preFunctionCall =
                    "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
            val args = listOf(LxmNil).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                    initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test copyWithin - incorrect at - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val variable = "testVariable"
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val targetVariable = "targetTestVariable"
            val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
            val targetTxt = target.joinToString(ListNode.elementSeparator)

            val preFunctionCall =
                    "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
            val args = listOf(targetVariable, LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                    initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test copyWithin - incorrect at - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val variable = "testVariable"
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val targetVariable = "targetTestVariable"
            val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
            val targetTxt = target.joinToString(ListNode.elementSeparator)

            val preFunctionCall =
                    "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
            val args = listOf(targetVariable, -1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                    initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test copyWithin - incorrect from - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val variable = "testVariable"
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val targetVariable = "targetTestVariable"
            val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
            val targetTxt = target.joinToString(ListNode.elementSeparator)

            val preFunctionCall =
                    "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
            val args = listOf(targetVariable, 2, LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                    initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test copyWithin - incorrect from - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val variable = "testVariable"
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val targetVariable = "targetTestVariable"
            val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
            val targetTxt = target.joinToString(ListNode.elementSeparator)

            val preFunctionCall =
                    "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
            val args = listOf(targetVariable, 2, -1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                    initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test copyWithin - incorrect count - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val variable = "testVariable"
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val targetVariable = "targetTestVariable"
            val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
            val targetTxt = target.joinToString(ListNode.elementSeparator)

            val preFunctionCall =
                    "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
            val args = listOf(targetVariable, 2, 2, LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                    initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test copyWithin - incorrect count - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val variable = "testVariable"
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val targetVariable = "targetTestVariable"
            val target = listOf(LxmFloat.Num0_5, LxmLogic.True, LxmNil, LxmInteger.Num1)
            val targetTxt = target.joinToString(ListNode.elementSeparator)

            val preFunctionCall =
                    "$targetVariable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$targetTxt${ListNode.endToken} \n $variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
            val args = listOf(targetVariable, 2, 2, -1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.CopyWithin}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                    initialVars = mapOf(variable to LxmNil, targetVariable to LxmNil)) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test push - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Push}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test push - with values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val additionElements = listOf(LxmNil, LxmLogic.False)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Push}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size + additionElements.size, oriList.actualListSize,
                    "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in additionElements.zip(oriList.getAllCells().drop(list.size))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test pop - without count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Pop}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(list.last(), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size - 1, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.dropLast(1).zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test pop - with count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val count = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Pop}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(count, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in list.takeLast(count).asReversed().zip(resList.getAllCells())) {
                Assertions.assertEquals(listElement, resElement, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size - count, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.dropLast(count).zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test pop - with count - count exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val count = 20

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Pop}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(list.size, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in list.asReversed().zip(resList.getAllCells())) {
                Assertions.assertEquals(listElement, resElement, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(0, oriList.actualListSize, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test pop - incorrect count - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Pop}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test pop - incorrect count - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(-1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Pop}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test unshift - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Unshift}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test unshift - with values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val additionElements = listOf(LxmNil, LxmLogic.False)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Unshift}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size + additionElements.size, oriList.actualListSize,
                    "The result is incorrect")

            for ((listElement, oriElement) in additionElements.zip(oriList.getAllCells().take(additionElements.size))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in list.zip(oriList.getAllCells().drop(additionElements.size))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test shift - without count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Shift}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(list.first(), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size - 1, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.drop(1).zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test shift - with count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val count = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Shift}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(count, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in list.take(count).zip(resList.getAllCells())) {
                Assertions.assertEquals(listElement, resElement, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size - count, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.drop(count).zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test shift - with count - count exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val count = 20

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Shift}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(list.size, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in list.zip(resList.getAllCells())) {
                Assertions.assertEquals(listElement, resElement, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(0, oriList.actualListSize, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test shift - incorrect count - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Shift}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test shift - incorrect count - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(-1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Shift}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test insert - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Insert}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test insert - with values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 2
        val additionElements = listOf(LxmNil, LxmLogic.False)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, *additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Insert}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size + additionElements.size, oriList.actualListSize,
                    "The result is incorrect")

            for ((listElement, oriElement) in list.take(at).zip(oriList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in additionElements.zip(oriList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(at).zip(
                    oriList.getAllCells().drop(at + additionElements.size))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test insert - with values - at exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 20
        val additionElements = listOf(LxmNil, LxmLogic.False)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, *additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Insert}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size + additionElements.size, oriList.actualListSize,
                    "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells().take(list.size))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in additionElements.zip(oriList.getAllCells().drop(list.size))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test insert - incorrect at - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Insert}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test insert - incorrect at - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(-1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Insert}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test replace - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 2
        val count = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size - count, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.take(at).zip(oriList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(at + count).zip(oriList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test replace - with values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 2
        val count = 2
        val additionElements = listOf(LxmNil, LxmLogic.False)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, count, *additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size - count + additionElements.size, oriList.actualListSize,
                    "The result is incorrect")

            for ((listElement, oriElement) in list.take(at).zip(oriList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in additionElements.zip(oriList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in list.drop(at + count).zip(oriList.getAllCells().drop(at + count))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test replace - with values - at exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 40
        val count = 2
        val additionElements = listOf(LxmNil, LxmLogic.False)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, count, *additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size + additionElements.size, oriList.actualListSize,
                    "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells().take(list.size))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in additionElements.zip(oriList.getAllCells().drop(list.size))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test replace - with values - count exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val at = 2
        val count = 40
        val additionElements = listOf(LxmNil, LxmLogic.False)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(at, count, *additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(at + additionElements.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.take(at).zip(oriList.getAllCells().take(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }

            for ((listElement, oriElement) in additionElements.zip(oriList.getAllCells().drop(at))) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test replace - incorrect at - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test replace - incorrect at - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(-1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test replace - incorrect count - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(2, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test replace - incorrect count - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(2, -1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Replace}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test reverse`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Reverse}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.reversed().zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test reverse - empty`() {
        val variable = "testVariable"
        val list = listOf<LexemPrimitive>()
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Reverse}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.reversed().zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has not been modified")
            }
        }
    }

    @Test
    fun `test slice - without count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val from = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(from).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(list.size - from, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in list.drop(from).zip(resList.getAllCells())) {
                Assertions.assertEquals(listElement, resElement, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test slice - without count - from exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val from = 10

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(from).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(0, resList.actualListSize, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test slice - with count`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val from = 2
        val count = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(from, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(count, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in list.drop(from).take(count).zip(resList.getAllCells())) {
                Assertions.assertEquals(listElement, resElement, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test slice - with count - from exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val from = 10
        val count = 2

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(from, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(0, resList.actualListSize, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    fun `test slice - with count - count exceeds`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val from = 2
        val count = 20

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(from, count).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(list.size - from, resList.actualListSize, "The result is incorrect")

            for ((listElement, resElement) in list.drop(from).zip(resList.getAllCells())) {
                Assertions.assertEquals(listElement, resElement, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(list.size, oriList.actualListSize, "The result is incorrect")

            for ((listElement, oriElement) in list.zip(oriList.getAllCells())) {
                Assertions.assertEquals(listElement, oriElement, "The original list has been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test joinToString - incorrect from - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test joinToString - incorrect from - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(-1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test joinToString - incorrect count - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(1, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    @Incorrect
    fun `test joinToString - incorrect count - negative`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(1, -1).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ListPrototype.Slice}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test clear`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ListNode.startToken}$listTxt${ListNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ListPrototype.Clear}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val oriList = context.getDereferencedProperty<LxmList>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmList")
            Assertions.assertEquals(0, oriList.actualListSize, "The result is incorrect")
        }
    }
}
