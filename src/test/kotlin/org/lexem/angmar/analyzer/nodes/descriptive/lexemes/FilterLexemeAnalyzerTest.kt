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

internal class FilterLexemeAnalyzerTest {
    @Test
    fun `test without next - match`() {
        val nodeName = "nodeName"
        val grammar = "${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val lxmNode = LxmNode(analyzer.memory, "createdNode", analyzer.text.saveCursor())

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNode, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode(analyzer.memory, "processedNode", analyzer.text.saveCursor())
        val childNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
        childNode.addToParent(analyzer.memory, parent)
        parent.addChildren(analyzer.memory, childNode)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parent)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        val newPosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger
        val resultRef = analyzer.memory.getLastFromStack() as LxmReference
        val result = resultRef.dereferenceAs<LxmNode>(analyzer.memory, toWrite = false)!!
        val resultParent = result.getParent(analyzer.memory, toWrite = false)!!

        Assertions.assertEquals(1, newPosition.primitive, "The new position is incorrect")
        Assertions.assertEquals(childNode.getPrimitive().position, resultRef.position, "The result is incorrect")
        Assertions.assertEquals(lxmNode.getPrimitive().position, resultParent.getPrimitive().position,
                "The parent is incorrect")
        Assertions.assertEquals(1, resultParent.getChildCount(analyzer.memory), "The parent children size is incorrect")
        Assertions.assertEquals(childNode.getPrimitive().position,
                parent.getFirstChild(analyzer.memory, toWrite = false)?.getPrimitive()?.position,
                "The parent child[0] is incorrect")

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        analyzer.memory.removeLastFromStack()

        // Remove the circular references of the nodes.
        childNode.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoreConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test without next - not match`() {
        val nodeName = "nodeName"
        val grammar = "${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val lxmNode = LxmNode(analyzer.memory, "createdNode", analyzer.text.saveCursor())

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNode, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode(analyzer.memory, "processedNode", analyzer.text.saveCursor())
        val childNode = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
        childNode.addToParent(analyzer.memory, parent)
        parent.addChildren(analyzer.memory, childNode)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parent)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)

        // Remove the circular references of the nodes.
        childNode.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoreConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negated without next - not match`() {
        val nodeName = "nodeName"
        val grammar =
                "${FilterLexemeNode.notOperator}${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val lxmNode = LxmNode(analyzer.memory, "createdNode", analyzer.text.saveCursor())

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNode, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode(analyzer.memory, "processedNode", analyzer.text.saveCursor())
        val childNode = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
        childNode.addToParent(analyzer.memory, parent)
        parent.addChildren(analyzer.memory, childNode)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parent)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        val newPosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger

        Assertions.assertEquals(0, newPosition.primitive, "The new position is incorrect")
        Assertions.assertEquals(LxmNil, analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        analyzer.memory.removeLastFromStack()

        // Remove the circular references of the nodes.
        childNode.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoreConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negated without next - match`() {
        val nodeName = "nodeName"
        val grammar =
                "${FilterLexemeNode.notOperator}${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val lxmNode = LxmNode(analyzer.memory, "createdNode", analyzer.text.saveCursor())

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNode, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode(analyzer.memory, "processedNode", analyzer.text.saveCursor())
        val childNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
        childNode.addToParent(analyzer.memory, parent)
        parent.addChildren(analyzer.memory, childNode)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parent)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)

        // Remove the circular references of the nodes.
        childNode.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoreConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test with next`() {
        val nodeName = "nodeName"
        val funName = "funName"
        val grammar =
                "${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken} ${FilterLexemeNode.nextAccessToken} $funName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        val lxmNode = LxmNode(analyzer.memory, "rootNode", analyzer.text.saveCursor())
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNode, isConstant = true)
        var executed = false
        val function = LxmFunction(analyzer.memory) { analyzer, _, _, _ ->
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
            val nodeRef = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.HiddenNode2Filter)!!
            val node = nodeRef.dereference(analyzer.memory, toWrite = false) as LxmNode

            Assertions.assertEquals(nodeName, node.name, "The name is incorrect")

            executed = true

            analyzer.memory.addToStackAsLast(nodeRef)

            true
        }
        context.setProperty(analyzer.memory, funName, function)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                LxmString.from("test"))

        // Prepare the node to filter.
        val parent = LxmNode(analyzer.memory, "processedNode", analyzer.text.saveCursor())
        val childNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
        childNode.addToParent(analyzer.memory, parent)
        parent.addChildren(analyzer.memory, childNode)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parent)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        val newPosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger
        val resultRef = analyzer.memory.getLastFromStack() as LxmReference
        val result = resultRef.dereferenceAs<LxmNode>(analyzer.memory, toWrite = false)!!
        val resultParent = result.getParent(analyzer.memory, toWrite = false)!!

        Assertions.assertTrue(executed, "The function has not been executed")
        Assertions.assertEquals(1, newPosition.primitive, "The new position is incorrect")
        Assertions.assertEquals(childNode.getPrimitive().position, resultRef.position, "The result is incorrect")
        Assertions.assertEquals(lxmNode.getPrimitive().position, resultParent.getPrimitive().position,
                "The parent is incorrect")
        Assertions.assertEquals(1, resultParent.getChildCount(analyzer.memory), "The parent children size is incorrect")
        Assertions.assertEquals(childNode.getPrimitive().position,
                resultParent.getFirstChild(analyzer.memory, toWrite = false)?.getPrimitive()?.position,
                "The parent child[0] is incorrect")

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        analyzer.memory.removeLastFromStack()

        // Remove the circular references of the nodes.
        childNode.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoreConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer,
                listOf(funName, AnalyzerCommons.Identifiers.Node, AnalyzerCommons.Identifiers.HiddenCurrentContextName))
    }

    @Test
    @Incorrect
    fun `test with next but incorrect returned value`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.FilterLexemWithNextRequiresANode) {
            val value = LxmInteger.Num10
            val nodeName = "nodeName"
            val funName = "funName"
            val grammar =
                    "${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken} ${FilterLexemeNode.nextAccessToken} $funName${FunctionCallNode.startToken}${FunctionCallNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            val lxmNode = LxmNode(analyzer.memory, "rootNode", analyzer.text.saveCursor())
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNode, isConstant = true)
            val function = LxmFunction(analyzer.memory) { analyzer, _, _, _ ->
                val node = AnalyzerCommons.getCurrentContextElement<LxmNode>(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenNode2Filter, toWrite = false)

                Assertions.assertEquals(nodeName, node.name, "The name is incorrect")

                analyzer.memory.addToStackAsLast(value)

                true
            }
            context.setProperty(analyzer.memory, funName, function)
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenCurrentContextName,
                    LxmString.from("test"))

            // Prepare the node to filter.
            val parent = LxmNode(analyzer.memory, "processedNode", analyzer.text.saveCursor())
            val childNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
            childNode.addToParent(analyzer.memory, parent)
            parent.addChildren(analyzer.memory, childNode)

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parent)
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
