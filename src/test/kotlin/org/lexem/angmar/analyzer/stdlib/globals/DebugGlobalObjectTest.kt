package org.lexem.angmar.analyzer.stdlib.globals

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class DebugGlobalObjectTest {
    @Test
    fun `test pause`() {
        val varName = "test"
        val varStmt = "$varName ${AssignOperatorNode.assignOperator} 5"
        val fnCall = "${DebugGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${DebugGlobalObject.Pause}"
        val grammar = "$fnCall${FunctionCallNode.startToken}${FunctionCallNode.endToken} \n $varStmt"
        val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
            LexemFileNode.parse(parser)
        }

        // Prepare context.
        var context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        context.setProperty(analyzer.memory, varName, LxmNil)

        analyzer.start(IOStringReader.from(""), timeoutInMilliseconds = Long.MAX_VALUE)

        context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val result = context.getPropertyValue(analyzer.memory, varName)
        Assertions.assertEquals(LxmNil, result, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test log`() {
        val value = "this is a test"
        val (stdOut, errOut) = TestUtils.handleLogs {
            val fnCall =
                    "${DebugGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${DebugGlobalObject.Log}"
            val fnArgs = "${StringNode.startToken}$value${StringNode.endToken}"
            val grammar = "$fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)

            TestUtils.checkEmptyStackAndContext(analyzer)
        }

        Assertions.assertTrue(errOut.isEmpty(), "The error output must be empty")
        Assertions.assertTrue(stdOut.contains(value), "The value is not contained")
    }

    @Test
    fun `test log with tag`() {
        val tag = "tag"
        val value = "this is a test"
        val (stdOut, errOut) = TestUtils.handleLogs {
            val fnCall =
                    "${DebugGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${DebugGlobalObject.Log}"
            val fnArgs =
                    "${StringNode.startToken}$value${StringNode.endToken}${FunctionCallNode.argumentSeparator}${StringNode.startToken}$tag${StringNode.endToken}"
            val grammar = "$fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)

            TestUtils.checkEmptyStackAndContext(analyzer)
        }

        Assertions.assertTrue(errOut.isEmpty(), "The error output must be empty")
        Assertions.assertTrue(stdOut.contains(value), "The value is not contained")
        Assertions.assertTrue(stdOut.contains(tag), "The tag is not contained")
    }

    @Test
    @Incorrect
    fun `test throw`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CustomError) {
            val value = "this is a test"
            val fnCall =
                    "${DebugGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${DebugGlobalObject.Throw}"
            val fnArgs = "${StringNode.startToken}$value${StringNode.endToken}"
            val grammar = "$fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)

            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test log with incorrect tag type`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val tag = 5
            val value = "this is a test"
            val fnCall =
                    "${DebugGlobalObject.ObjectName}${AccessExplicitMemberNode.accessToken}${DebugGlobalObject.Log}"
            val fnArgs = "${StringNode.startToken}$value${StringNode.endToken}${FunctionCallNode.argumentSeparator}$tag"
            val grammar = "$fnCall${FunctionCallNode.startToken}$fnArgs${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)

            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }
}
