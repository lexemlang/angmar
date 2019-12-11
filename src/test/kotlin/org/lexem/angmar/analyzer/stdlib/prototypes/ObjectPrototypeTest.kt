package org.lexem.angmar.analyzer.stdlib.prototypes

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ObjectPrototypeTest {
    @Test
    fun `test freeze`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }
        val resultValue = LxmNil

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.Freeze}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            Assertions.assertTrue(objOri.isImmutable, "The isImmutable property is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test isFrozen`(isOk: Boolean) {
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }
        val resultValue = LxmLogic.from(isOk)

        val valueTxt = "${ParenthesisExpressionNode.startToken}${if (isOk) {
            ObjectNode.constantToken
        } else {
            ""
        }}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.IsFrozen}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test isPropertyFrozen`(isOk: Boolean) {
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
            "${if (isOk) {
                ObjectNode.constantToken
            } else {
                ""
            }}${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
        }
        val resultValue = LxmLogic.from(isOk)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("${StringNode.startToken}a${StringNode.endToken}").joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.IsPropertyFrozen}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test isPropertyFrozen - incorrect property - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
            val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
                "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
            }

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.IsPropertyFrozen}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test freezeProperties`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }
        val resultValue = LxmNil

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val args = listOf("${StringNode.startToken}a${StringNode.endToken}").joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.FreezeProperties}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            Assertions.assertTrue(objOri.getPropertyDescriptor(analyzer.memory, "a")?.isConstant ?: false,
                    "The a property is incorrect")
            Assertions.assertFalse(objOri.getPropertyDescriptor(analyzer.memory, "b")?.isConstant ?: true,
                    "The b property is incorrect")
        }
    }

    @Test
    fun `test freezeProperties - empty`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }
        val resultValue = LxmNil

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.FreezeProperties}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            Assertions.assertFalse(objOri.getPropertyDescriptor(analyzer.memory, "a")?.isConstant ?: true,
                    "The a property is incorrect")
            Assertions.assertFalse(objOri.getPropertyDescriptor(analyzer.memory, "b")?.isConstant ?: true,
                    "The b property is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test freezeProperties - incorrect property - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
            val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
                "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
            }

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.FreezeProperties}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test freezeAllProperties`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }
        val resultValue = LxmNil

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.FreezeAllProperties}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            for ((key, value) in obj) {
                val descriptor = objOri.getOwnPropertyDescriptor(analyzer.memory, key) ?: throw Error(
                        "The original has been modified")
                Assertions.assertTrue(descriptor.isConstant, "The isConstant property is incorrect")
                Assertions.assertEquals(value, descriptor.value, "The isConstant property is incorrect")
            }
        }
    }

    @Test
    fun `test removeProperties`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }
        val resultValue = LxmNil

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val args = listOf("${StringNode.startToken}a${StringNode.endToken}").joinToString(
                FunctionCallNode.argumentSeparator)
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.RemoveProperties}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            Assertions.assertNull(objOri.getPropertyDescriptor(analyzer.memory, "a"), "The a property is incorrect")
            Assertions.assertNotNull(objOri.getPropertyDescriptor(analyzer.memory, "b"), "The b property is incorrect")
        }
    }

    @Test
    fun `test removeProperties - empty`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }
        val resultValue = LxmNil

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.RemoveProperties}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            Assertions.assertNotNull(objOri.getPropertyDescriptor(analyzer.memory, "a"), "The a property is incorrect")
            Assertions.assertNotNull(objOri.getPropertyDescriptor(analyzer.memory, "b"), "The b property is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test removeProperties - incorrect property - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
            val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
                "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
            }

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.RemoveProperties}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @Test
    fun `test getOwnPropertyKeys`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.GetOwnPropertyKeys}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(obj.size, resList.actualListSize, "The result is incorrect")

            for (element in resList.getAllCells()) {
                element as? LxmString ?: throw Error("All elements in the result list must be LxmString")
                Assertions.assertTrue(element.primitive in obj, "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            for ((key, value) in obj) {
                val descriptor = objOri.getOwnPropertyDescriptor(analyzer.memory, key) ?: throw Error(
                        "The original has been modified")
                Assertions.assertFalse(descriptor.isConstant, "The isConstant property is incorrect")
                Assertions.assertEquals(value, descriptor.value, "The isConstant property is incorrect")
            }
        }
    }

    @Test
    fun `test getOwnPropertyValues`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.GetOwnPropertyValues}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(obj.size, resList.actualListSize, "The result is incorrect")

            for (element in resList.getAllCells()) {
                Assertions.assertTrue(obj.containsValue(element), "The result list is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            for ((key, value) in obj) {
                val descriptor = objOri.getOwnPropertyDescriptor(analyzer.memory, key) ?: throw Error(
                        "The original has been modified")
                Assertions.assertFalse(descriptor.isConstant, "The isConstant property is incorrect")
                Assertions.assertEquals(value, descriptor.value, "The isConstant property is incorrect")
            }
        }
    }

    @Test
    fun `test getOwnProperties`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.GetOwnProperties}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resList = result?.dereference(analyzer.memory) as? LxmList ?: throw Error("The result must be LxmList")
            Assertions.assertEquals(obj.size, resList.actualListSize, "The result is incorrect")

            for (element in resList.getAllCells()) {
                val elementObj = element.dereference(analyzer.memory) as? LxmObject ?: throw Error(
                        "All elements in the result list must be LxmObject")
                val key = elementObj.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Key) as? LxmString
                        ?: throw Error("All keys must be LxmString")
                val value =
                        elementObj.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Value) ?: throw Error(
                                "All values must exist")

                Assertions.assertTrue(key.primitive in obj, "The key property is incorrect")
                Assertions.assertEquals(obj[key.primitive], value, "The value property is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            for ((key, value) in obj) {
                val descriptor = objOri.getOwnPropertyDescriptor(analyzer.memory, key) ?: throw Error(
                        "The original has been modified")
                Assertions.assertFalse(descriptor.isConstant, "The isConstant property is incorrect")
                Assertions.assertEquals(value, descriptor.value, "The isConstant property is incorrect")
            }
        }
    }

    @Test
    fun `test ownPropertiesToMap`() {
        val variable = "testVariable"
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(
                ObjectNode.elementSeparator) { "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}" }

        val preFunctionCall =
                "$variable ${AssignOperatorNode.assignOperator} ${ObjectNode.startToken}$objTxt${ObjectNode.endToken}"
        val fnCall =
                "$variable${AccessExplicitMemberNode.accessToken}${ObjectPrototype.OwnPropertiesToMap}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall, preFunctionCall,
                initialVars = mapOf(variable to LxmNil)) { analyzer, result ->
            val resMap = result?.dereference(analyzer.memory) as? LxmMap ?: throw Error("The result must be LxmMap")
            Assertions.assertEquals(obj.size, resMap.getSize(), "The result is incorrect")

            for (prop in resMap.getAllProperties().flatMap { it.value }) {
                val key = prop.key as? LxmString ?: throw Error("All keys must be LxmString")
                val value = prop.value

                Assertions.assertTrue(key.primitive in obj, "The key property is incorrect")
                Assertions.assertEquals(obj[key.primitive], value, "The value property is incorrect")
            }

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val objOri = context.getDereferencedProperty<LxmObject>(analyzer.memory, variable) ?: throw Error(
                    "The variable must contain a LxmObject")

            for ((key, value) in obj) {
                val descriptor = objOri.getOwnPropertyDescriptor(analyzer.memory, key) ?: throw Error(
                        "The original has been modified")
                Assertions.assertFalse(descriptor.isConstant, "The isConstant property is incorrect")
                Assertions.assertEquals(value, descriptor.value, "The isConstant property is incorrect")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test containsAnyOwnProperty`(isOk: Boolean) {
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
            "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
        }
        val resultValue = LxmLogic.from(isOk)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("${StringNode.startToken}x${StringNode.endToken}", if (isOk) {
            "${StringNode.startToken}a${StringNode.endToken}"
        } else {
            "${StringNode.startToken}y${StringNode.endToken}"
        }).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.ContainsAnyOwnProperty}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    fun `test containsAnyOwnProperty - empty`() {
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
            "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
        }
        val resultValue = LxmLogic.False

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.ContainsAnyOwnProperty}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test containsAnyOwnProperty - incorrect property - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
            val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
                "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
            }

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.ContainsAnyOwnProperty}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test containsAllOwnProperties`(isOk: Boolean) {
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
            "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
        }
        val resultValue = LxmLogic.from(isOk)

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
        val args = listOf("${StringNode.startToken}b${StringNode.endToken}", if (isOk) {
            "${StringNode.startToken}a${StringNode.endToken}"
        } else {
            "${StringNode.startToken}y${StringNode.endToken}"
        }).joinToString(FunctionCallNode.argumentSeparator)
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.ContainsAllOwnProperties}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    fun `test containsAllOwnProperties - empty`() {
        val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
        val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
            "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
        }
        val resultValue = LxmLogic.True

        val valueTxt =
                "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
        val fnCall =
                "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.ContainsAllOwnProperties}${FunctionCallNode.startToken}${FunctionCallNode.endToken}"

        TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            Assertions.assertEquals(resultValue, result, "The result is incorrect")
        }
    }

    @Test
    @Incorrect
    fun `test containsAllOwnProperties - incorrect property - type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val obj = mapOf("a" to LxmInteger.Num1, "b" to LxmFloat.Num0_5)
            val objTxt = obj.toList().joinToString(ObjectNode.elementSeparator) {
                "${it.first}${ObjectElementNode.keyValueSeparator}${it.second}"
            }

            val valueTxt =
                    "${ParenthesisExpressionNode.startToken}${ObjectNode.startToken}$objTxt${ObjectNode.endToken}${ParenthesisExpressionNode.endToken}"
            val args = listOf(LxmLogic.True).joinToString(FunctionCallNode.argumentSeparator)
            val fnCall =
                    "$valueTxt${AccessExplicitMemberNode.accessToken}${ObjectPrototype.ContainsAllOwnProperties}${FunctionCallNode.startToken}$args${FunctionCallNode.endToken}"

            TestUtils.e2eTestExecutingExpression(fnCall) { analyzer, result ->
            }
        }
    }
}
