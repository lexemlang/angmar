package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.utils.*

internal class AdditionFilterLexemeAnalyzerTest {
    @Test
    fun `test addition - without next`() {
        val nodeName = "nodeName"
        val grammar = "${AdditionFilterLexemeNode.startToken}$nodeName${AdditionFilterLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AdditionFilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lxmNode = LxmNode("rootNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef, isConstant = true)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack().dereference(analyzer.memory) as? LxmNode ?: throw Error(
                "The result must be a LxmNode")
        val parentRef = result.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Parent) as LxmReference
        val parentChildren = result.getParent(analyzer.memory)!!.getChildren(analyzer.memory)
        Assertions.assertEquals(lxmNodeRef.position, parentRef.position, "The parent node is incorrect")
        Assertions.assertEquals(1, parentChildren.actualListSize, "The number of children is incorrect")
        Assertions.assertEquals((analyzer.memory.getLastFromStack() as LxmReference).position,
                (parentChildren.getCell(analyzer.memory, 0) as LxmReference).position,
                "The child has not been included in the parent")

        // Remove FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        analyzer.memory.removeLastFromStack()

        // Remove the circular references of the nodes.
        result.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Parent, LxmNil, ignoringConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test addition - with next`() {
        val nodeName = "nodeName"
        val funName = "funName"
        val grammar =
                "${AdditionFilterLexemeNode.startToken}$nodeName${AdditionFilterLexemeNode.endToken} ${AdditionFilterLexemeNode.nextAccessToken} $funName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = AdditionFilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lxmNode = LxmNode("rootNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef, isConstant = true)
        var executed = false
        val function = LxmFunction { analyzer, _, _, _ ->
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val nodeRef = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.HiddenNode2Filter)!!
            val node = nodeRef.dereference(analyzer.memory) as LxmNode

            Assertions.assertEquals(nodeName, node.name, "The name is incorrect")

            executed = true

            analyzer.memory.addToStackAsLast(nodeRef)

            true
        }
        val functionRef = analyzer.memory.add(function)
        context.setProperty(analyzer.memory, funName, functionRef)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack().dereference(analyzer.memory) as LxmNode
        Assertions.assertEquals(nodeName, result.name, "The result is incorrect")
        Assertions.assertTrue(executed, "The function has not been executec")

        // Remove FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        analyzer.memory.removeLastFromStack()

        // Remove the circular references of the nodes.
        result.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Parent, LxmNil, ignoringConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(funName, AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }


    @Test
    @Incorrect
    fun `test addition - with next but incorrect returned value`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.AdditionLexemWithNextRequiresANode) {
            val value = LxmInteger.Num10
            val nodeName = "nodeName"
            val funName = "funName"
            val grammar =
                    "${AdditionFilterLexemeNode.startToken}$nodeName${AdditionFilterLexemeNode.endToken} ${AdditionFilterLexemeNode.nextAccessToken} $funName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(grammar, parserFunction = AdditionFilterLexemeNode.Companion::parse)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val function = LxmFunction { analyzer, _, _, _ ->
                val node = AnalyzerCommons.getCurrentContextElement<LxmNode>(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenNode2Filter)

                Assertions.assertEquals(nodeName, node.name, "The name is incorrect")

                analyzer.memory.addToStackAsLast(value)

                true
            }
            val functionRef = analyzer.memory.add(function)
            context.setProperty(analyzer.memory, funName, functionRef)
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                    LxmString.from("test"))

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
