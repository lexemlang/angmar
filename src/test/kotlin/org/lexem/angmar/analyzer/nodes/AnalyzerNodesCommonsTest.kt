package org.lexem.angmar.analyzer.nodes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class AnalyzerNodesCommonsTest {
    @Test
    fun `test call internal function`() {
        val text = LogicNode.falseLiteral
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = LogicNode.Companion::parse)

        val arguments = LxmArguments(analyzer.memory)
        val function = LxmFunction(analyzer.memory) { _, _, _, _ -> true }

        val returnSignal = 45
        AnalyzerNodesCommons.callFunction(analyzer, function, arguments, CompiledNode.Companion.EmptyCompiledNode,
                LxmCodePoint(CompiledNode.Companion.EmptyCompiledNode, returnSignal,
                        CompiledNode.Companion.EmptyCompiledNode, ""))

        // Assert status of the analyzer.
        val stackFunction = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Function)
        val stackArguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments)
                .dereference(analyzer.memory, toWrite = false)
        val stackReturnPosition =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint) as LxmCodePoint

        Assertions.assertEquals(function.getPrimitive(), stackFunction, "The stackFunction is incorrect")
        Assertions.assertEquals(arguments, stackArguments, "The stackArguments is incorrect")
        Assertions.assertEquals(CompiledNode.Companion.EmptyCompiledNode, stackReturnPosition.node,
                "The stackReturnPosition.node is incorrect")
        Assertions.assertEquals(returnSignal, stackReturnPosition.signal, "The stackReturnPosition.signal is incorrect")

        Assertions.assertEquals(LexemAnalyzer.ProcessStatus.Forward, analyzer.processStatus, "The status is incorrect")
        Assertions.assertEquals(InternalFunctionCallCompiled, analyzer.nextNode, "The next node is incorrect")
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
