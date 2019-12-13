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

internal class MapPrototypeTest {
    @Test
    fun `test size`() {
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)
        val resultValue = map.size

        val valueTxt = "${ParenthesisExpressionNode.startToken}${mapToString(map)}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${MapPrototype.Size}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            result as? LxmInteger ?: throw Error("The result must be LxmInteger")
            Assertions.assertEquals(resultValue, result.primitive, "The result is incorrect")
        }
    }

    @Test
    fun `test freeze`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)
        val resultValue = LxmNil

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Freeze}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test isFrozen`(isOk: Boolean) {
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)
        val resultValue = LxmLogic.from(isOk)

        val valueTxt = "${ParenthesisExpressionNode.startToken}${mapToString(map,
                isConst = isOk)}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${MapPrototype.IsFrozen}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test every`(isOk: Boolean) {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val value2Check = if (isOk) {
            LxmFloat.Num0_5
        } else {
            LxmInteger.Num1
        }

        val checkFunctionKeyArg = "key"
        val checkFunctionValueArg = "value"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionValueArg ${RelationalExpressionNode.greaterOrEqualThanOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionValueArg${FunctionParameterListNode.parameterSeparator}$checkFunctionKeyArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Every}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test every - empty`() {
        val variable = "testVariable"

        val value2Check = 0
        val checkFunctionKeyArg = "key"
        val checkFunctionValueArg = "value"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionKeyArg ${RelationalExpressionNode.identityOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionValueArg${FunctionParameterListNode.parameterSeparator}$checkFunctionKeyArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString()}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Every}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(0, mapOri.getSize(), "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test every - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${mapToString(map)}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${MapPrototype.Every}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test filter`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmInteger.Num_1)

        val value2Check = LxmInteger.Num1
        val checkFunctionKeyArg = "key"
        val checkFunctionValueArg = "value"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionValueArg ${RelationalExpressionNode.greaterOrEqualThanOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionValueArg${FunctionParameterListNode.parameterSeparator}$checkFunctionKeyArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Filter}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resMap = result?.dereference(analyzer.memory) as? LxmMap ?: throw Error("The result must be LxmMap")
            Assertions.assertEquals(1, resMap.getSize(), "The result is incorrect")

            val resultMap = map.filter { it.value.primitive >= value2Check.primitive }
            for (prop in resMap.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in resultMap, "The original map has been modified")
                Assertions.assertEquals(resultMap[prop.key], prop.value, "The original map has been modified")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test filter - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${mapToString(map)}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${MapPrototype.Filter}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test forEach`() {
        val variable = "testVariable"
        val variableAccumulator = "testVariableAccumulator"
        val map = mapOf(LxmLogic.True to LxmInteger.Num2, LxmString.Nil to LxmInteger.Num_1)

        val checkFunctionKeyArg = "key"
        val checkFunctionValueArg = "value"
        val checkFunctionStatement =
                "$variableAccumulator ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} $checkFunctionValueArg"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionValueArg${FunctionParameterListNode.parameterSeparator}$checkFunctionKeyArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.ForEach}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil, variableAccumulator to LxmInteger.Num0)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }

            val accumulator = context.getPropertyValue(analyzer.memory, variableAccumulator)
            Assertions.assertEquals(LxmInteger.Num1, accumulator, "The variable is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test forEach - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${mapToString(map)}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${MapPrototype.ForEach}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test containsAnyKey - with values`(isOk: Boolean) {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmInteger.Num10 to LxmFloat.Num0_5)

        val additionElements = listOf(LxmFloat.Num0_5, if (isOk) {
            LxmInteger.Num10
        } else {
            LxmLogic.False
        })

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.ContainsAnyKey}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test containsAnyKey - without values`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmInteger.Num10 to LxmFloat.Num0_5)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.ContainsAnyKey}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test containsAllKey - with values`(isOk: Boolean) {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmInteger.Num10 to LxmFloat.Num0_5)

        val additionElements = listOf(LxmLogic.True, if (isOk) {
            LxmInteger.Num10
        } else {
            LxmLogic.False
        })

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.ContainsAllKeys}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test containsAllKey - without values`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmInteger.Num10 to LxmFloat.Num0_5)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.ContainsAllKeys}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.True, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test map`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmInteger.Num_1)

        val value2Check = LxmInteger.Num1
        val checkFunctionKeyArg = "key"
        val checkFunctionValueArg = "value"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionValueArg ${AdditiveExpressionNode.additionOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionValueArg${FunctionParameterListNode.parameterSeparator}$checkFunctionKeyArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Map}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resMap = result?.dereference(analyzer.memory) as? LxmMap ?: throw Error("The result must be LxmMap")
            Assertions.assertEquals(map.size, resMap.getSize(), "The result is incorrect")

            val resultMap = map.mapValues { LxmInteger.from(it.value.primitive + 1) }
            for (prop in resMap.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in resultMap, "The original map has been modified")
                Assertions.assertEquals(resultMap[prop.key], prop.value, "The original map has been modified")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test map - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${mapToString(map)}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${MapPrototype.Map}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test reduce`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num2, LxmString.Nil to LxmInteger.Num_1)

        val checkFunctionAccArg = "acc"
        val checkFunctionKeyArg = "key"
        val checkFunctionValueArg = "value"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionValueArg ${AdditiveExpressionNode.additionOperator} $checkFunctionAccArg"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionAccArg${FunctionParameterListNode.parameterSeparator}$checkFunctionValueArg${FunctionParameterListNode.parameterSeparator}$checkFunctionKeyArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(0, checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Reduce}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmInteger.Num1, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    @Incorrect
    fun `test reduce - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${mapToString(map)}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${MapPrototype.Reduce}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test any`(isOk: Boolean) {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmNil to LxmFloat.Num0_5)

        val value2Check = if (isOk) {
            LxmNil
        } else {
            LxmInteger.Num2
        }

        val checkFunctionKeyArg = "key"
        val checkFunctionValueArg = "value"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionKeyArg ${RelationalExpressionNode.identityOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionValueArg${FunctionParameterListNode.parameterSeparator}$checkFunctionKeyArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Any}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.from(isOk), result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test any - empty`() {
        val variable = "testVariable"

        val value2Check = 0
        val checkFunctionKeyArg = "key"
        val checkFunctionValueArg = "value"
        val checkFunctionStatement =
                "${ControlWithExpressionStmtNode.returnKeyword} $checkFunctionKeyArg ${RelationalExpressionNode.identityOperator} $value2Check"
        val checkFunction =
                "${FunctionNode.keyword}${FunctionParameterListNode.startToken}$checkFunctionValueArg${FunctionParameterListNode.parameterSeparator}$checkFunctionKeyArg${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}$checkFunctionStatement${BlockStmtNode.endToken}"

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString()}"
        val args = listOf(checkFunction).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Any}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmLogic.False, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(0, mapOri.getSize(), "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test any - incorrect fn - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${mapToString(map)}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.False).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${MapPrototype.Any}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test put`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val additionElements = listOf(LxmLogic.False, LxmInteger.Num0)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Put}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size + 1, mapOri.getSize(), "The result is incorrect")

            val resProps = mapOri.getAllProperties().flatMap { it.value }
            for ((key, value) in map) {
                Assertions.assertTrue(key in resProps.map { it.key }, "The original map has been modified")
                Assertions.assertEquals(value, resProps.find { it.key == key }?.value,
                        "The original map has been modified")
            }

            Assertions.assertTrue(additionElements[0] in resProps.map { it.key }, "The original map has been modified")
            Assertions.assertEquals(additionElements[1], resProps.find { it.key == additionElements[0] }?.value,
                    "The original map has been modified")
        }
    }

    @Test
    fun `test remove - without values`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf<LexemPrimitive>().joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Remove}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test remove - with values`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val additionElements = listOf(LxmLogic.True)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val args = listOf(*additionElements.toTypedArray()).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Remove}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val resultMap = map.filter { it.key !in additionElements }
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size - 1, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in resultMap, "The original map has been modified")
                Assertions.assertEquals(resultMap[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test toList`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.ToList}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(map.size, resList.actualListSize, "The result is incorrect")

            for (element in resList.getAllCells()) {
                val obj = element.dereference(analyzer.memory) as? LxmObject ?: throw Error(
                        "All elements must be LxmObjects")
                val key = obj.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key)
                val value = obj.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value)

                Assertions.assertTrue(key in map, "The result is incorrect")
                Assertions.assertEquals(map[key], value, "The result is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test toObject`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.ToObject}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resObj =
                    result?.dereference(analyzer.memory) as? LxmObject ?: throw Error("The result must be LxmObject")

            for ((key, value) in resObj.getAllIterableProperties()) {
                Assertions.assertTrue(LxmString.from(key) in map, "The result is incorrect")
                Assertions.assertEquals(map[LxmString.from(key)], value.value, "The result is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test keys`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Keys}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(map.size, resList.actualListSize, "The result is incorrect")

            for (element in resList.getAllCells()) {
                Assertions.assertTrue(element in map.keys, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test values`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Values}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(map.size, resList.actualListSize, "The result is incorrect")

            for (element in resList.getAllCells()) {
                Assertions.assertTrue(element in map.values, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(map.size, mapOri.getSize(), "The result is incorrect")

            for (prop in mapOri.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map, "The original map has been modified")
                Assertions.assertEquals(map[prop.key], prop.value, "The original map has been modified")
            }
        }
    }

    @Test
    fun `test clear`() {
        val variable = "testVariable"
        val map = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)

        val preFunctionCall = "$variable ${AssignOperatorNode.assignOperator} ${mapToString(map)}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${MapPrototype.Clear}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(LxmNil, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val mapOri = context.getDereferencedProperty<LxmMap>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmMap")
            Assertions.assertEquals(0, mapOri.getSize(), "The result is incorrect")
        }
    }

    @Test
    fun `test add`() {
        val map1 = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)
        val map2 = mapOf(LxmString.Nil to LxmFloat.Num0_5, LxmInteger.Num0 to LxmLogic.True)

        val fnCall = "${mapToString(map1)}${AdditiveExpressionNode.additionOperator}${mapToString(map2)}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val map = result?.dereference(analyzer.memory) as? LxmMap ?: throw Error("The result must be LxmMap")
            Assertions.assertEquals(3, map.getSize(), "The size of the result is incorrect")

            for (prop in map.getAllProperties().flatMap { it.value }) {
                when (prop.key) {
                    in map2 -> {
                        Assertions.assertEquals(map2[prop.key], prop.value, "The value of '${prop.key}' is incorrect")
                    }
                    in map1 -> {
                        Assertions.assertEquals(map1[prop.key], prop.value, "The value of '${prop.key}' is incorrect")
                    }
                    else -> throw Error("The result is incorrect")
                }
            }
        }
    }

    @Test
    fun `test sub`() {
        val map1 = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)
        val map2 = mapOf(LxmString.Nil to LxmFloat.Num0_5, LxmInteger.Num0 to LxmLogic.True)

        val fnCall = "${mapToString(map1)}${AdditiveExpressionNode.subtractionOperator}${mapToString(map2)}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val map = result?.dereference(analyzer.memory) as? LxmMap ?: throw Error("The result must be LxmMap")
            Assertions.assertEquals(1, map.getSize(), "The size of the result is incorrect")

            for (prop in map.getAllProperties().flatMap { it.value }) {
                when (prop.key) {
                    in map1 -> {
                        Assertions.assertEquals(map1[prop.key], prop.value, "The value of '${prop.key}' is incorrect")
                    }
                    else -> throw Error("The result is incorrect")
                }
            }
        }
    }

    @Test
    fun `test logicalAnd`() {
        val map1 = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)
        val map2 = mapOf(LxmString.Nil to LxmFloat.Num0_5, LxmInteger.Num0 to LxmLogic.True)

        val fnCall = "${mapToString(map1)}${LogicalExpressionNode.andOperator}${mapToString(map2)}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val map = result?.dereference(analyzer.memory) as? LxmMap ?: throw Error("The result must be LxmMap")
            Assertions.assertEquals(1, map.getSize(), "The size of the result is incorrect")

            for (prop in map.getAllProperties().flatMap { it.value }) {
                Assertions.assertTrue(prop.key in map1 && prop.key in map2,
                        "The result is incorrect${prop.key}' is incorrect")
                Assertions.assertEquals(map2[prop.key], prop.value, "The value of '${prop.key}' is incorrect")
            }
        }
    }

    @Test
    fun `test logicalOr`() {
        val map1 = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)
        val map2 = mapOf(LxmString.Nil to LxmFloat.Num0_5, LxmInteger.Num0 to LxmLogic.True)

        val fnCall = "${mapToString(map1)}${LogicalExpressionNode.orOperator}${mapToString(map2)}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val map = result?.dereference(analyzer.memory) as? LxmMap ?: throw Error("The result must be LxmMap")
            Assertions.assertEquals(3, map.getSize(), "The size of the result is incorrect")

            for (prop in map.getAllProperties().flatMap { it.value }) {
                when (prop.key) {
                    in map2 -> {
                        Assertions.assertEquals(map2[prop.key], prop.value, "The value of '${prop.key}' is incorrect")
                    }
                    in map1 -> {
                        Assertions.assertEquals(map1[prop.key], prop.value, "The value of '${prop.key}' is incorrect")
                    }
                    else -> throw Error("The result is incorrect")
                }
            }
        }
    }

    @Test
    fun `test logicalXor`() {
        val map1 = mapOf(LxmLogic.True to LxmInteger.Num1, LxmString.Nil to LxmFloat.Num0_5)
        val map2 = mapOf(LxmString.Nil to LxmFloat.Num0_5, LxmInteger.Num0 to LxmLogic.True)

        val fnCall = "${mapToString(map1)}${LogicalExpressionNode.xorOperator}${mapToString(map2)}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            val map = result?.dereference(analyzer.memory) as? LxmMap ?: throw Error("The result must be LxmMap")
            Assertions.assertEquals(2, map.getSize(), "The size of the result is incorrect")

            for (prop in map.getAllProperties().flatMap { it.value }) {
                when (prop.key) {
                    in map2 -> {
                        Assertions.assertTrue(prop.key !in map1, "The result is incorrect")
                        Assertions.assertEquals(map2[prop.key], prop.value, "The value of '${prop.key}' is incorrect")
                    }
                    in map1 -> {
                        Assertions.assertEquals(map1[prop.key], prop.value, "The value of '${prop.key}' is incorrect")
                    }
                    else -> throw Error("The result is incorrect")
                }
            }
        }
    }

    // AUXILIARY METHODS ------------------------------------------------------

    private fun mapToString(map: Map<LexemPrimitive, LexemPrimitive> = emptyMap(), isConst: Boolean = false) =
            StringBuilder().apply {
                append(MapNode.macroName)

                if (isConst) {
                    append(MapNode.constantToken)
                }

                append(MapNode.startToken)

                val res = map.entries.joinToString(MapNode.elementSeparator) {
                    "${if (it.key is LxmString) {
                        "${StringNode.startToken}${(it.key as LxmString).primitive}${StringNode.endToken}"
                    } else {
                        it.key.toString()
                    }}${MapElementNode.keyValueSeparator}${it.value}"
                }

                append("$res${MapNode.endToken}")
            }.toString()
}
