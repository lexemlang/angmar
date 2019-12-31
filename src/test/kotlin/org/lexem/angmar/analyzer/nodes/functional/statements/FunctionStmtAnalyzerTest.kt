package org.lexem.angmar.analyzer.nodes.functional.statements

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

internal class FunctionStmtAnalyzerTest {
    @Test
    fun `test normal without params`() {
        val fnName = "fn"
        val text = "${FunctionStmtNode.keyword} $fnName ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val fn = context.getPropertyValue(fnName)?.dereference(analyzer.memory, toWrite = false) as? LxmFunction
                ?: throw Error("The result must be a LxmFunction")

        Assertions.assertEquals(fnName, fn.name, "The name property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(fnName))
    }

    @Test
    fun `test normal with params`() {
        val fnName = "fn"
        val text =
                "${FunctionStmtNode.keyword} $fnName ${FunctionParameterListNode.startToken}${FunctionParameterListNode.endToken} ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = FunctionStmtNode.Companion::parse)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val fn = context.getPropertyValue(fnName)?.dereference(analyzer.memory, toWrite = false) as? LxmFunction
                ?: throw Error("The result must be a LxmFunction")

        Assertions.assertEquals(fnName, fn.name, "The name property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(fnName))
    }

    @Test
    fun `test call without argument list`() {
        val varName = "test"
        val fnName = "fn"
        val value = LxmInteger.Num10
        val text = let {
            val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val body =
                    "${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $value ${BlockStmtNode.endToken}"
            val fn = "${FunctionStmtNode.keyword} $fnName $body"
            "${BlockStmtNode.startToken} $fn \n $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(varName, LxmNil)
        context.setProperty(AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        Assertions.assertEquals(value, finalContext.getPropertyValue(varName), "The $varName is incorrect")

        // Remove the function cyclic reference.
        analyzer.memory.spatialGarbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test call with argument list`() {
        val varName = "test"
        val fnName = "fn"
        val paramName = "param"
        val value = LxmInteger.Num10
        val text = let {
            val callFn = "$fnName${FunctionCallNode.startToken} $value ${FunctionCallNode.endToken}"
            val body =
                    "${BlockStmtNode.startToken} $varName ${AssignOperatorNode.assignOperator} $paramName ${BlockStmtNode.endToken}"
            val fn =
                    "${FunctionStmtNode.keyword} $fnName ${FunctionParameterListNode.startToken} $paramName ${FunctionParameterListNode.endToken} $body"
            "${BlockStmtNode.startToken} $fn \n $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(varName, LxmNil)
        context.setProperty(AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        Assertions.assertEquals(value, finalContext.getPropertyValue(varName), "The $varName is incorrect")

        // Remove the function cyclic reference.
        analyzer.memory.spatialGarbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test default returned value`() {
        val varName = "test"
        val fnName = "fn"
        val text = let {
            val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val body = "${BlockStmtNode.startToken} ${BlockStmtNode.endToken}"
            val fn = "${FunctionStmtNode.keyword} $fnName $body"
            "${BlockStmtNode.startToken} $fn \n $varName ${AssignOperatorNode.assignOperator} $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(varName, LxmNil)
        context.setProperty(AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        Assertions.assertEquals(LxmNil, finalContext.getPropertyValue(varName), "The $varName is incorrect")

        // Remove the function cyclic reference.
        analyzer.memory.spatialGarbageCollect()

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(varName, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    fun `test return control signals`() {
        val keyword = ControlWithExpressionStmtNode.returnKeyword
        val varName = "test"
        val fnName = "fn"
        val value = LxmInteger.Num1
        val text = let {
            val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val body = "${BlockStmtNode.startToken} $keyword $value ${BlockStmtNode.endToken}"
            val fn = "${FunctionStmtNode.keyword} $fnName $body"
            "${BlockStmtNode.startToken} $fn \n $varName ${AssignOperatorNode.assignOperator} $callFn ${BlockStmtNode.endToken}"
        }
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

        // Prepare the context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        context.setProperty(varName, LxmNil)
        context.setProperty(AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val finalContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        Assertions.assertEquals(value, finalContext.getPropertyValue(varName), "The $varName is incorrect")

        // Remove the function cyclic reference.
        analyzer.memory.spatialGarbageCollect()

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
            val text = let {
                val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
                val body = "${BlockStmtNode.startToken} $keyword ${BlockStmtNode.endToken}"
                val fn = "${FunctionStmtNode.keyword} $fnName $body"
                "${BlockStmtNode.startToken} $fn \n $callFn ${BlockStmtNode.endToken}"
            }
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

            // Prepare the context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty(AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.from("test"))

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
            val text = let {
                val callFn = "$fnName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
                val body =
                        "${BlockStmtNode.startToken}${BlockStmtNode.tagPrefix}$tagName $keyword${BlockStmtNode.tagPrefix}$tagName ${BlockStmtNode.endToken}"
                val fn = "${FunctionStmtNode.keyword} $fnName $body"
                "${BlockStmtNode.startToken} $fn \n $callFn ${BlockStmtNode.endToken}"
            }
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = BlockStmtNode.Companion::parse)

            // Prepare the context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            context.setProperty(AnalyzerCommons.Identifiers.HiddenCurrentContextName, LxmString.from("test"))

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
