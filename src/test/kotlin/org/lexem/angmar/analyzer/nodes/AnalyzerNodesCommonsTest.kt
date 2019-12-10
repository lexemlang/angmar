package org.lexem.angmar.analyzer.nodes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class AnalyzerNodesCommonsTest {
    @Test
    fun `test call internal function`() {
        val text = LogicNode.falseLiteral
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicNode.Companion::parse)

        val arguments = LxmArguments(analyzer.memory)
        val argumentReference = analyzer.memory.add(arguments)

        val function = LxmFunction { _, _, _, _ -> true }
        val functionRef = analyzer.memory.add(function)

        val returnSignal = 45
        AnalyzerNodesCommons.callFunction(analyzer, functionRef, argumentReference,
                ParserNode.Companion.EmptyParserNode,
                LxmCodePoint(ParserNode.Companion.EmptyParserNode, returnSignal, ParserNode.Companion.EmptyParserNode,
                        ""))

        // Assert status of the analyzer.
        val stackFunction = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Function)
        val stackArguments =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(analyzer.memory)
        val stackReturnPosition =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint) as LxmCodePoint

        Assertions.assertEquals(functionRef, stackFunction, "The stackFunction is incorrect")
        Assertions.assertEquals(arguments, stackArguments, "The stackArguments is incorrect")
        Assertions.assertEquals(ParserNode.Companion.EmptyParserNode, stackReturnPosition.node,
                "The stackReturnPosition.node is incorrect")
        Assertions.assertEquals(returnSignal, stackReturnPosition.signal, "The stackReturnPosition.signal is incorrect")

        Assertions.assertEquals(LexemAnalyzer.ProcessStatus.Forward, analyzer.processStatus, "The status is incorrect")
        Assertions.assertEquals(InternalFunctionCallNode, analyzer.nextNode, "The next node is incorrect")
        Assertions.assertEquals(AnalyzerNodesCommons.signalCallFunction, analyzer.signal, "The signal is incorrect")

        // Remove Function, Arguments and ReturnCodePoint from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Function)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint)
    }

    @Test
    fun `test call function`() {
        /** Done in [FunctionAnalyzerTest], [FunctionStmtAnalyzerTest] and [ObjectSimplificationAnalyzerTest] **/
    }
}
