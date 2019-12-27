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

internal class SetPrototypeTest {
    @Test
    fun `test size`() {
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)
        val resultValue = list.size

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.Size}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { _, result ->
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Freeze}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")

            Assertions.assertTrue(set.isConstant, "The isConstant property is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test isFrozen`(isOk: Boolean) {
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)
        val resultValue = LxmLogic.from(isOk)

        val valueTxt = "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${if (isOk) {
            ListNode.constantToken
        } else {
            ""
        }}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.IsFrozen}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { _, result ->
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Every}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
            }
        }
    }

    @Test
    fun `test every - empty`() {
        val variable = "testVariable"

        val value2Check = 0
        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${RelationalExpressionNode.greaterOrEqualThanOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Every}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            Assertions.assertEquals(0, set.size, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test every - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.Every}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Filter}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resultList = list.filter { it > 2 }
            val resSet = result?.dereference(analyzer.memory, toWrite = false) as? LxmSet ?: throw Error(
                    "The result must be LxmSet")
            Assertions.assertEquals(resultList.size, resSet.size, "The result is incorrect")

            for (value in resSet.getAllValues()) {
                value as? LxmInteger ?: throw Error("All elements in the result list must be LxmInteger")

                if (value.primitive !in list) {
                    throw Error("The result list is incorrect")
                }
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if ((prop as LxmInteger).primitive !in list) {
                    throw Error("The original set has changed")
                }
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
                    "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.Filter}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.ForEach}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil, variableAccumulator to LxmInteger.Num0)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if ((prop as LxmInteger).primitive !in list) {
                    throw Error("The original set has changed")
                }
            }

            val accumulator =
                    context.getDereferencedProperty<LxmInteger>(analyzer.memory, variableAccumulator, toWrite = false)
                            ?: throw Error("The variable must contain a LxmInteger")
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
                    "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.ForEach}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test find`(isOk: Boolean) {
        val variable = "testVariable"
        val list = listOf(LxmFloat.Num0_5, LxmInteger.Num1, LxmNil, LxmInteger.Num10, LxmLogic.True)
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Find}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            if (isOk) {
                Assertions.assertEquals(LxmInteger.Num1, result, "The result is incorrect")
            } else {
                Assertions.assertEquals(LxmNil, result, "The result is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
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
                    "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.Find}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    fun `test containsAny - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.ContainsAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.ContainsAny}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
            }
        }
    }

    @Test
    fun `test containsAll - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.ContainsAll}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.ContainsAll}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
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
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${AdditiveExpressionNode.additionOperator} 10"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Map}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resSet = result?.dereference(analyzer.memory, toWrite = false) as? LxmSet ?: throw Error(
                    "The result must be LxmSet")
            Assertions.assertEquals(list.size, resSet.size, "The result is incorrect")

            val resList = list.map { it + 10 }
            for (prop in resSet.getAllValues()) {
                prop as? LxmInteger ?: throw Error("All elements in the result list must be LxmInteger")
                if (prop.primitive !in resList) {
                    throw Error("The result list is incorrect")
                }
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if ((prop as LxmInteger).primitive !in list) {
                    throw Error("The original set has changed")
                }
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
                    "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.Map}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(0, checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Reduce}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be a LxmInteger")
            Assertions.assertEquals(list.sum(), result.primitive, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if ((prop as LxmInteger).primitive !in list) {
                    throw Error("The original set has changed")
                }
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
                    "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(2, LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.Reduce}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Any}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
            }
        }
    }

    @Test
    fun `test any - empty`() {
        val variable = "testVariable"

        val value2Check = 0
        val checkFunctionArg = "it"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionArg ${RelationalExpressionNode.greaterOrEqualThanOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}${ListNode.endToken}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Any}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            Assertions.assertEquals(0, set.size, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test any - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
            val listTxt = list.joinToString(ListNode.elementSeparator)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${SetPrototype.Any}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { _, _ ->
            }
        }
    }

    @Test
    fun `test put - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Put}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
            }
        }
    }

    @Test
    fun `test put - with values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val additionElements = listOf(LxmLogic.False, LxmInteger.Num0)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Put}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size + additionElements.size, set.size, "The result is incorrect")

            val resultList = list + additionElements
            for (prop in setValues) {
                if (prop !in resultList) {
                    throw Error("The original set has changed")
                }
            }
        }
    }

    @Test
    fun `test remove - without values`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Remove}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
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
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Remove}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val resultList = list.filter { it != LxmLogic.True }
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(resultList.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
            }

            for (prop in additionElements) {
                if (prop in setValues) {
                    throw Error("The removed values are contained in the set")
                }
            }
        }
    }

    @Test
    fun `test toList`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.ToList}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory, toWrite = false) as? LxmList ?: throw Error(
                    "The result must be LxmList")
            Assertions.assertEquals(list.size, resList.size, "The result is incorrect")

            for (element in resList.getAllCells()) {
                if (element !in list) {
                    throw Error("The result list is incorrect")
                }
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            val setValues = set.getAllValues()
            Assertions.assertEquals(list.size, set.size, "The result is incorrect")

            for (prop in setValues) {
                if (prop !in list) {
                    throw Error("The original set has changed")
                }
            }
        }
    }

    @Test
    fun `test clear`() {
        val variable = "testVariable"
        val list = listOf(LxmInteger.Num1, LxmFloat.Num0_5, LxmNil, LxmLogic.True, LxmInteger.Num10)
        val listTxt = list.joinToString(ListNode.elementSeparator)

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${SetNode.macroName}${ListNode.startToken}$listTxt${ListNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${SetPrototype.Clear}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val set =
                    context.getDereferencedProperty<LxmSet>(analyzer.memory, variable, toWrite = false) ?: throw Error(
                            "The variable must contain a LxmSet")
            Assertions.assertEquals(0, set.size, "The result is incorrect")
        }
    }

    @Test
    fun `test add`() {
        val list1 = listOf(LxmInteger.Num1, LxmFloat.Num0_5)
        val list2 = listOf(LxmFloat.Num0_5, LxmLogic.True)
        val list1Txt = list1.joinToString(ListNode.elementSeparator)
        val list2Txt = list2.joinToString(ListNode.elementSeparator)

        val fnCall =
                "${SetNode.macroName}${ListNode.startToken}$list1Txt${ListNode.endToken}${AdditiveExpressionNode.additionOperator}${SetNode.macroName}${ListNode.startToken}$list2Txt${ListNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val set = result?.dereference(analyzer.memory, toWrite = false) as? LxmSet ?: throw Error(
                    "The result must be LxmSet")
            Assertions.assertEquals(3, set.size, "The size of the result is incorrect")

            for (i in set.getAllValues()) {
                Assertions.assertTrue(i in list1 || i in list2, "The result is incorrect")
            }
        }
    }

    @Test
    fun `test sub`() {
        val list1 = listOf(LxmInteger.Num1, LxmFloat.Num0_5)
        val list2 = listOf(LxmFloat.Num0_5, LxmLogic.True)
        val list1Txt = list1.joinToString(ListNode.elementSeparator)
        val list2Txt = list2.joinToString(ListNode.elementSeparator)

        val fnCall =
                "${SetNode.macroName}${ListNode.startToken}$list1Txt${ListNode.endToken}${AdditiveExpressionNode.subtractionOperator}${SetNode.macroName}${ListNode.startToken}$list2Txt${ListNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val set = result?.dereference(analyzer.memory, toWrite = false) as? LxmSet ?: throw Error(
                    "The result must be LxmSet")
            Assertions.assertEquals(1, set.size, "The size of the result is incorrect")

            for (i in set.getAllValues()) {
                Assertions.assertTrue(i in list1 && i !in list2, "The result is incorrect")
            }
        }
    }

    @Test
    fun `test logicalAnd`() {
        val list1 = listOf(LxmInteger.Num1, LxmFloat.Num0_5)
        val list2 = listOf(LxmFloat.Num0_5, LxmLogic.True)
        val list1Txt = list1.joinToString(ListNode.elementSeparator)
        val list2Txt = list2.joinToString(ListNode.elementSeparator)

        val fnCall =
                "${SetNode.macroName}${ListNode.startToken}$list1Txt${ListNode.endToken}${LogicalExpressionNode.andOperator}${SetNode.macroName}${ListNode.startToken}$list2Txt${ListNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val set = result?.dereference(analyzer.memory, toWrite = false) as? LxmSet ?: throw Error(
                    "The result must be LxmSet")
            Assertions.assertEquals(1, set.size, "The size of the result is incorrect")

            for (i in set.getAllValues()) {
                Assertions.assertTrue(i in list1 && i in list2, "The result is incorrect")
            }
        }
    }

    @Test
    fun `test logicalOr`() {
        val list1 = listOf(LxmInteger.Num1, LxmFloat.Num0_5)
        val list2 = listOf(LxmFloat.Num0_5, LxmLogic.True)
        val list1Txt = list1.joinToString(ListNode.elementSeparator)
        val list2Txt = list2.joinToString(ListNode.elementSeparator)

        val fnCall =
                "${SetNode.macroName}${ListNode.startToken}$list1Txt${ListNode.endToken}${LogicalExpressionNode.orOperator}${SetNode.macroName}${ListNode.startToken}$list2Txt${ListNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val set = result?.dereference(analyzer.memory, toWrite = false) as? LxmSet ?: throw Error(
                    "The result must be LxmSet")
            Assertions.assertEquals(3, set.size, "The size of the result is incorrect")

            for (i in set.getAllValues()) {
                Assertions.assertTrue(i in list1 || i in list2, "The result is incorrect")
            }
        }
    }

    @Test
    fun `test logicalXor`() {
        val list1 = listOf(LxmInteger.Num1, LxmFloat.Num0_5)
        val list2 = listOf(LxmFloat.Num0_5, LxmLogic.True)
        val list1Txt = list1.joinToString(ListNode.elementSeparator)
        val list2Txt = list2.joinToString(ListNode.elementSeparator)

        val fnCall =
                "${SetNode.macroName}${ListNode.startToken}$list1Txt${ListNode.endToken}${LogicalExpressionNode.xorOperator}${SetNode.macroName}${ListNode.startToken}$list2Txt${ListNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val set = result?.dereference(analyzer.memory, toWrite = false) as? LxmSet ?: throw Error(
                    "The result must be LxmSet")
            Assertions.assertEquals(2, set.size, "The size of the result is incorrect")

            for (i in set.getAllValues()) {
                Assertions.assertTrue((i in list1 && i !in list2) || (i !in list1 && i in list2),
                        "The result is incorrect")
            }
        }
    }
}
