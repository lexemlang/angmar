package org.lexem.angmar.analyzer.nodes.literals

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
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ObjectSimplificationAnalyzerTest {
    @Test
    fun `test normal`() {
        val key = "test"
        val text = "$key ${ObjectNode.elementSeparator}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ObjectSimplificationNode.Companion::parse)

        // Prepare stack.
        val obj = LxmObject(analyzer.memory)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, obj)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmReference ?: throw Error(
                        "The result must be a LxmReference")
        val objDeref = resultRef.dereferenceAs<LxmObject>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmObject")
        val property = objDeref.getPropertyDescriptor(analyzer.memory, key)!!
        property.value.dereference(analyzer.memory, toWrite = false) as? LxmFunction ?: throw Error(
                "The element must be a LxmInteger")

        Assertions.assertFalse(property.isConstant, "The isConstant property is incorrect")

        // Remove Accumulator and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test constant function`() {
        val key = "test"
        val text =
                "${ObjectSimplificationNode.constantToken}$key${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken}${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = ObjectSimplificationNode.Companion::parse)

        // Prepare stack.
        val obj = LxmObject(analyzer.memory)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Accumulator, obj)

        TestUtils.processAndCheckEmpty(analyzer)

        val resultRef =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as? LxmReference ?: throw Error(
                        "The result must be a LxmReference")
        val objDeref = resultRef.dereferenceAs<LxmObject>(analyzer.memory, toWrite = false) ?: throw Error(
                "The result must be a LxmObject")
        val property = objDeref.getPropertyDescriptor(analyzer.memory, key)!!
        property.value.dereference(analyzer.memory, toWrite = false) as? LxmFunction ?: throw Error(
                "The element must be a LxmInteger")

        Assertions.assertTrue(property.isConstant, "The isConstant property is incorrect")

        // Remove Accumulator and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Accumulator)

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test call function`() {
        val varName = "test"
        val objVarName = "obj"
        val fnName = "fn"
        val value = LxmInteger.Num10
        val text = let {
            val callFn =
                    "$objVarName${AccessExplicitMemberNode.accessToken}$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val body =
                    "${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $value ${BlockStmtNode.endToken}"
            val fn = "$fnName${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken} $body"
            val obj = "${ObjectNode.startToken}$fn${ObjectNode.endToken}"
            val assign =
                    "${VarDeclarationStmtNode.variableKeyword} $objVarName ${VarDeclarationStmtNode.assignOperator} $obj"
            "${BlockStmtNode.startToken} $assign \n $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmNil)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        Assertions.assertEquals(value, finalContext.getPropertyValue(analyzer.memory, varName),
                "The $varName is incorrect")

        // Remove the dangling references.
        analyzer.memory.lastNode.garbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals without expression`(keyword: String) {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal) {
            val fnName = "fn"
            val objVarName = "obj"
            val text = let {
                val callFn =
                        "$objVarName${AccessExplicitMemberNode.accessToken}$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
                val body = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
                val fn = "$fnName${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken} $body"
                val obj = "${ObjectNode.startToken}$fn${ObjectNode.endToken}"
                val assign =
                        "${VarDeclarationStmtNode.variableKeyword} $objVarName ${VarDeclarationStmtNode.assignOperator} $obj"
                "${BlockStmtNode.startToken} $assign \n $callFn ${BlockStmtNode.endToken}"
            }
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

            // Prepare the context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                    LxmString.from("test"))

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = [ControlWithoutExpressionStmtNode.exitKeyword, ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword, ControlWithoutExpressionStmtNode.restartKeyword])
    fun `test control signals with tag and without expression`(keyword: String) {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal) {
            val fnName = "fn"
            val tagName = "tag"
            val objVarName = "obj"
            val text = let {
                val callFn =
                        "$objVarName${AccessExplicitMemberNode.accessToken}$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
                val body =
                        "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
                val fn = "$fnName${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken} $body"
                val obj = "${ObjectNode.startToken}$fn${ObjectNode.endToken}"
                val assign =
                        "${VarDeclarationStmtNode.variableKeyword} $objVarName ${VarDeclarationStmtNode.assignOperator} $obj"
                "${BlockStmtNode.startToken} $assign \n $callFn ${BlockStmtNode.endToken}"
            }
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

            // Prepare the context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                    LxmString.from("test"))

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    fun `test return control signal`() {
        val keyword = ControlWithExpressionStmtNode.returnKeyword
        val varName = "test"
        val fnName = "fn"
        val objVarName = "obj"
        val value = LxmInteger.Num1
        val text = let {
            val callFn =
                    "$objVarName${AccessExplicitMemberNode.accessToken}$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val body = "${BlockStmtNode.startToken} $keyword $value ${BlockStmtNode.endToken}"
            val fn = "$fnName${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken} $body"
            val obj = "${ObjectNode.startToken}$fn${ObjectNode.endToken}"
            val assign =
                    "${VarDeclarationStmtNode.variableKeyword} $objVarName ${VarDeclarationStmtNode.assignOperator} $obj"
            "${BlockStmtNode.startToken} $assign \n $varName ${AssignOperatorNode.assignOperator} $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(analyzer.memory, varName, LxmNil)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        Assertions.assertEquals(value, finalContext.getPropertyValue(analyzer.memory, varName),
                "The $varName is incorrect")

        // Remove the function cyclic reference.
        analyzer.memory.lastNode.garbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }
}
