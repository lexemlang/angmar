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
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lxmNode = LxmNode("createdNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode("processedNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val parentRef = analyzer.memory.add(parent)
        val children = parent.getChildren(analyzer.memory)
        val childNode = LxmNode(nodeName, analyzer.text.saveCursor(), parentRef, analyzer.memory)
        val childNodeRef = analyzer.memory.add(childNode)
        children.addCell(analyzer.memory, childNodeRef, ignoreConstant = true)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parentRef)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        val newPosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger
        val resultRef = analyzer.memory.getLastFromStack() as LxmReference
        val result = resultRef.dereferenceAs<LxmNode>(analyzer.memory)!!
        val resultParentRef = result.getParentReference(analyzer.memory)!!
        val resultParent = resultParentRef.dereferenceAs<LxmNode>(analyzer.memory)!!
        val resultParentChildren = resultParent.getChildrenAsList(analyzer.memory)

        Assertions.assertEquals(1, newPosition.primitive, "The new position is incorrect")
        Assertions.assertEquals(childNodeRef.position, resultRef.position, "The result is incorrect")
        Assertions.assertEquals(lxmNodeRef.position, resultParentRef.position, "The parent is incorrect")
        Assertions.assertEquals(1, resultParentChildren.size, "The parent children size is incorrect")
        Assertions.assertEquals(childNodeRef.position, (resultParentChildren[0] as LxmReference).position,
                "The parent child[0] is incorrect")

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        analyzer.memory.removeLastFromStack()

        // Remove the circular references of the nodes.
        childNodeRef.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoringConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test without next - not match`() {
        val nodeName = "nodeName"
        val grammar = "${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lxmNode = LxmNode("createdNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode("processedNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val parentRef = analyzer.memory.add(parent)
        val children = parent.getChildren(analyzer.memory)
        val childNode = LxmNode(nodeName + "x", analyzer.text.saveCursor(), parentRef, analyzer.memory)
        val childNodeRef = analyzer.memory.add(childNode)
        children.addCell(analyzer.memory, childNodeRef, ignoreConstant = true)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parentRef)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)

        // Remove the circular references of the nodes.
        childNodeRef.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoringConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negated without next - not match`() {
        val nodeName = "nodeName"
        val grammar =
                "${FilterLexemeNode.notOperator}${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lxmNode = LxmNode("createdNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode("processedNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val parentRef = analyzer.memory.add(parent)
        val children = parent.getChildren(analyzer.memory)
        val childNode = LxmNode(nodeName + "x", analyzer.text.saveCursor(), parentRef, analyzer.memory)
        val childNodeRef = analyzer.memory.add(childNode)
        children.addCell(analyzer.memory, childNodeRef, ignoreConstant = true)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parentRef)
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
        childNodeRef.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoringConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.Node))
    }

    @Test
    fun `test negated without next - match`() {
        val nodeName = "nodeName"
        val grammar =
                "${FilterLexemeNode.notOperator}${FilterLexemeNode.startToken}$nodeName${FilterLexemeNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = FilterLexemeNode.Companion::parse)

        // Prepare context.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lxmNode = LxmNode("createdNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef, isConstant = true)

        // Prepare the node to filter.
        val parent = LxmNode("processedNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val parentRef = analyzer.memory.add(parent)
        val children = parent.getChildren(analyzer.memory)
        val childNode = LxmNode(nodeName, analyzer.text.saveCursor(), parentRef, analyzer.memory)
        val childNodeRef = analyzer.memory.add(childNode)
        children.addCell(analyzer.memory, childNodeRef, ignoreConstant = true)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parentRef)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer, status = LexemAnalyzer.ProcessStatus.Backward, bigNodeCount = 0)

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)

        // Remove the circular references of the nodes.
        childNodeRef.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoringConstant = true)

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
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lxmNode = LxmNode("rootNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)
        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef, isConstant = true)
        var executed = false
        val internalFunction = LxmInternalFunction { analyzer, _, _ ->
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val nodeRef = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.HiddenNode2Filter)!!
            val node = nodeRef.dereference(analyzer.memory) as LxmNode

            Assertions.assertEquals(nodeName, node.name, "The name is incorrect")

            executed = true

            analyzer.memory.addToStackAsLast(nodeRef)

            true
        }
        context.setProperty(analyzer.memory, funName, internalFunction)

        // Prepare the node to filter.
        val parent = LxmNode("processedNode", analyzer.text.saveCursor(), null, analyzer.memory)
        val parentRef = analyzer.memory.add(parent)
        val children = parent.getChildren(analyzer.memory)
        val childNode = LxmNode(nodeName, analyzer.text.saveCursor(), parentRef, analyzer.memory)
        val childNodeRef = analyzer.memory.add(childNode)
        children.addCell(analyzer.memory, childNodeRef, ignoreConstant = true)

        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parentRef)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        val newPosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger
        val resultRef = analyzer.memory.getLastFromStack() as LxmReference
        val result = resultRef.dereferenceAs<LxmNode>(analyzer.memory)!!
        val resultParentRef = result.getParentReference(analyzer.memory)!!
        val resultParent = resultParentRef.dereferenceAs<LxmNode>(analyzer.memory)!!
        val resultParentChildren = resultParent.getChildrenAsList(analyzer.memory)

        Assertions.assertTrue(executed, "The function has not been executed")
        Assertions.assertEquals(1, newPosition.primitive, "The new position is incorrect")
        Assertions.assertEquals(childNodeRef.position, resultRef.position, "The result is incorrect")
        Assertions.assertEquals(lxmNodeRef.position, resultParentRef.position, "The parent is incorrect")
        Assertions.assertEquals(1, resultParentChildren.size, "The parent children size is incorrect")
        Assertions.assertEquals(childNodeRef.position, (resultParentChildren[0] as LxmReference).position,
                "The parent child[0] is incorrect")

        // Remove FilterNode, FilterNodePosition and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        analyzer.memory.removeLastFromStack()

        // Remove the circular references of the nodes.
        childNodeRef.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                AnalyzerCommons.Identifiers.Parent, LxmNil, ignoringConstant = true)

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(funName, AnalyzerCommons.Identifiers.Node))
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
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val lxmNode = LxmNode("rootNode", analyzer.text.saveCursor(), null, analyzer.memory)
            val lxmNodeRef = analyzer.memory.add(lxmNode)
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef, isConstant = true)
            val internalFunction = LxmInternalFunction { analyzer, _, _ ->
                val node = AnalyzerCommons.getCurrentContextElement<LxmNode>(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenNode2Filter)

                Assertions.assertEquals(nodeName, node.name, "The name is incorrect")

                analyzer.memory.addToStackAsLast(value)

                true
            }
            context.setProperty(analyzer.memory, funName, internalFunction)

            // Prepare the node to filter.
            val parent = LxmNode("processedNode", analyzer.text.saveCursor(), null, analyzer.memory)
            val parentRef = analyzer.memory.add(parent)
            val children = parent.getChildren(analyzer.memory)
            val childNode = LxmNode(nodeName, analyzer.text.saveCursor(), parentRef, analyzer.memory)
            val childNodeRef = analyzer.memory.add(childNode)
            children.addCell(analyzer.memory, childNodeRef, ignoreConstant = true)

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, parentRef)
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
