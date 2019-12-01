package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.selectors.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class MethodSelectorAnalyzerTest {
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test root method`(isOk: Boolean) {
        val methodName = AnalyzerCommons.SelectorMethods.Root
        val grammar = "${MethodSelectorNode.relationalToken}$methodName"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmParentRef = analyzer.memory.add(lxmParent)
        val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), if (isOk) {
            null
        } else {
            lxmParentRef
        }, analyzer.memory)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        if (isOk) {
            lxmParentRef.increaseReferences(analyzer.memory)
            lxmParentRef.decreaseReferences(analyzer.memory)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test empty method`(isOk: Boolean) {
        val methodName = AnalyzerCommons.SelectorMethods.Empty
        val grammar = "${MethodSelectorNode.relationalToken}$methodName"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)

        if (!isOk) {
            val lxmChild = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
            val lxmChildRef = analyzer.memory.add(lxmChild)
            lxmNode.getChildren(analyzer.memory).addCell(analyzer.memory, lxmChildRef, ignoreConstant = true)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test firstChild method with condition`(type: Int) {
        val methodName = AnalyzerCommons.SelectorMethods.FirstChild
        val grammar = "${MethodSelectorNode.relationalToken}$methodName"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmParentRef = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
                null
            }
            // Incorrect - no first
            1 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNodeAux = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory)
                        .addCell(analyzer.memory, analyzer.memory.add(lxmNodeAux), ignoreConstant = true)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            else -> {
                null
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        if (type != 0) {
            lxmParentRef!!.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                    AnalyzerCommons.Identifiers.Children, LxmNil, ignoringConstant = true)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test lastChild method with condition`(type: Int) {
        val methodName = AnalyzerCommons.SelectorMethods.LastChild
        val grammar = "${MethodSelectorNode.relationalToken}$methodName"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmParentRef = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
                null
            }
            // Incorrect - no last
            1 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNodeAux = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)
                lxmParent.getChildren(analyzer.memory)
                        .addCell(analyzer.memory, analyzer.memory.add(lxmNodeAux), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            else -> {
                null
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        if (type != 0) {
            lxmParentRef!!.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                    AnalyzerCommons.Identifiers.Children, LxmNil, ignoringConstant = true)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test childCount method with condition`(isOk: Boolean) {
        val methodName = AnalyzerCommons.SelectorMethods.ChildCount
        val condition =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName} ${RelationalExpressionNode.identityOperator} 1"
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${PropertyBlockSelectorNode.startToken}$condition${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        if (isOk) {
            val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
            val lxmNodeRef = analyzer.memory.add(lxmNode)
            val lxmNodeAux = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
            lxmNode.getChildren(analyzer.memory)
                    .addCell(analyzer.memory, analyzer.memory.add(lxmNodeAux), ignoreConstant = true)

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
        } else {
            val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
            val lxmNodeRef = analyzer.memory.add(lxmNode)

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test nthChild method with condition`(type: Int) {
        val methodName = AnalyzerCommons.SelectorMethods.NthChild
        val condition =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName} ${RelationalExpressionNode.identityOperator} 1"
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${PropertyBlockSelectorNode.startToken}$condition${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmParentRef = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
                null
            }
            // Incorrect - no at 1
            1 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNodeAux = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory)
                        .addCell(analyzer.memory, analyzer.memory.add(lxmNodeAux), ignoreConstant = true)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            else -> {
                null
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        if (type != 0) {
            lxmParentRef!!.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                    AnalyzerCommons.Identifiers.Children, LxmNil, ignoringConstant = true)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test parent method with condition`(type: Int) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.Parent
        val condition =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.Name} ${RelationalExpressionNode.identityOperator} ${StringNode.startToken}$nodeName${StringNode.endToken}"
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${PropertyBlockSelectorNode.startToken}$condition${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
            }
            // Incorrect - no same name
            1 -> {
                val lxmParent = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test allChildren method with condition`(type: Int) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.AllChildren
        val condition =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.Name} ${RelationalExpressionNode.identityOperator} ${StringNode.startToken}$nodeName${StringNode.endToken}"
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${PropertyBlockSelectorNode.startToken}$condition${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        when (type) {
            // Correct - no children
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
            }
            // Correct - all with the name
            1 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
            // Incorrect - one without the name
            2 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type != 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test anyChild method with condition`(type: Int) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.AnyChild
        val condition =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.Name} ${RelationalExpressionNode.identityOperator} ${StringNode.startToken}$nodeName${StringNode.endToken}"
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${PropertyBlockSelectorNode.startToken}$condition${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        when (type) {
            // Incorrect - no children
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
            }
            // Incorrect - no one with the name
            1 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
            // Correct - one with name
            2 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test node method with condition`(isOk: Boolean) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.Node
        val condition =
                "${AnalyzerCommons.Identifiers.DefaultPropertyName}${AccessExplicitMemberNode.accessToken}${AnalyzerCommons.Identifiers.Name} ${RelationalExpressionNode.identityOperator} ${StringNode.startToken}$nodeName${StringNode.endToken}"
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${PropertyBlockSelectorNode.startToken}$condition${PropertyBlockSelectorNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        if (isOk) {
            val lxmNode = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
            val lxmNodeRef = analyzer.memory.add(lxmNode)

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
        } else {
            val lxmNode = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
            val lxmNodeRef = analyzer.memory.add(lxmNode)

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test parent method with selector`(type: Int) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.Parent
        val selector = nodeName
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${MethodSelectorNode.selectorStartToken}$selector${MethodSelectorNode.selectorEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmParentRef = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
                null
            }
            // Incorrect - parent with different name
            1 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            else -> {
                null
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        if (type != 0) {
            lxmParentRef!!.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                    AnalyzerCommons.Identifiers.Children, LxmNil, ignoringConstant = true)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test allChildren method with selector`(type: Int) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.AllChildren
        val selector = nodeName
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${MethodSelectorNode.selectorStartToken}$selector${MethodSelectorNode.selectorEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        when (type) {
            // Correct - no children
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
            }
            // Correct - all with the name
            1 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
            // Incorrect - one without the name
            2 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type != 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test anyChild method with selector`(type: Int) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.AnyChild
        val selector = nodeName
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${MethodSelectorNode.selectorStartToken}$selector${MethodSelectorNode.selectorEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        when (type) {
            // Incorrect - no children
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
            }
            // Incorrect - no one with the name
            1 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
            // Correct - one with name
            2 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test node method with selector`(isOk: Boolean) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.Node
        val selector = nodeName
        val grammar =
                "${MethodSelectorNode.relationalToken}$methodName${MethodSelectorNode.selectorStartToken}$selector${MethodSelectorNode.selectorEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        if (isOk) {
            val lxmNode = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
        } else {
            val lxmNode = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `test negated method without argument`(isOk: Boolean) {
        val methodName = AnalyzerCommons.SelectorMethods.Empty
        val grammar = "${MethodSelectorNode.relationalToken}${MethodSelectorNode.notOperator}$methodName"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
        val lxmNodeRef = analyzer.memory.add(lxmNode)
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)

        if (isOk) {
            val lxmChild = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
            val lxmChildRef = analyzer.memory.add(lxmChild)
            lxmNode.getChildren(analyzer.memory).addCell(analyzer.memory, lxmChildRef, ignoreConstant = true)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test negated method with condition`(type: Int) {
        val methodName = AnalyzerCommons.SelectorMethods.FirstChild
        val grammar = "${MethodSelectorNode.relationalToken}${MethodSelectorNode.notOperator}$methodName"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        val lxmParentRef = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
                null
            }
            // Incorrect - no first
            1 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNodeAux = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory)
                        .addCell(analyzer.memory, analyzer.memory.add(lxmNodeAux), ignoreConstant = true)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode("root", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmParentRef = analyzer.memory.add(lxmParent)
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), lxmParentRef, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                lxmParent.getChildren(analyzer.memory).addCell(analyzer.memory, lxmNodeRef, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
                lxmParentRef
            }
            else -> {
                null
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type != 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        if (type != 0) {
            lxmParentRef!!.dereferenceAs<LxmNode>(analyzer.memory)!!.setProperty(analyzer.memory,
                    AnalyzerCommons.Identifiers.Children, LxmNil, ignoringConstant = true)
        }

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2])
    fun `test negated method with selector`(type: Int) {
        val nodeName = "test"
        val methodName = AnalyzerCommons.SelectorMethods.AllChildren
        val selector = nodeName
        val grammar =
                "${MethodSelectorNode.relationalToken}${MethodSelectorNode.notOperator}$methodName${MethodSelectorNode.selectorStartToken}$selector${MethodSelectorNode.selectorEndToken}"
        val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

        // Prepare stack.
        when (type) {
            // Correct - no children
            0 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, analyzer.memory.add(lxmNode))
            }
            // Correct - all with the name
            1 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
            // Incorrect - one without the name
            2 -> {
                val lxmNode = LxmNode("nodeName", analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmNodeRef = analyzer.memory.add(lxmNode)
                val lxmAux1 = LxmNode(nodeName, analyzer.text.saveCursor(), null, analyzer.memory)
                val lxmAux2 = LxmNode(nodeName + "x", analyzer.text.saveCursor(), null, analyzer.memory)
                val children = lxmNode.getChildren(analyzer.memory)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux1), ignoreConstant = true)
                children.addCell(analyzer.memory, analyzer.memory.add(lxmAux2), ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)
            }
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    @Incorrect
    fun `test incorrect method without argument - require selector`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments) {
            val methodName = AnalyzerCommons.SelectorMethods.Parent
            val grammar = "${MethodSelectorNode.relationalToken}$methodName"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect method without argument - require condition`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments) {
            val methodName = AnalyzerCommons.SelectorMethods.ChildCount
            val grammar = "${MethodSelectorNode.relationalToken}$methodName"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect method with selector - does not require argument`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments) {
            val methodName = AnalyzerCommons.SelectorMethods.Empty
            val grammar =
                    "${MethodSelectorNode.relationalToken}$methodName${MethodSelectorNode.selectorStartToken}selectorName${MethodSelectorNode.selectorEndToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect method with selector - require condition`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments) {
            val methodName = AnalyzerCommons.SelectorMethods.ChildCount
            val grammar =
                    "${MethodSelectorNode.relationalToken}$methodName${MethodSelectorNode.selectorStartToken}selectorName${MethodSelectorNode.selectorEndToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect method with condition - does not require argument`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments) {
            val methodName = AnalyzerCommons.SelectorMethods.Empty
            val grammar =
                    "${MethodSelectorNode.relationalToken}$methodName${PropertyBlockSelectorNode.startToken}selectorName${PropertyBlockSelectorNode.endToken}"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    // Currently there is no method that requires only a selector.
    //    @Test
    //    @Incorrect
    //    fun `test incorrect method with condition - require selector`() {
    //        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments) {
    //            val methodName = AnalyzerCommons.SelectorMethods.Parent
    //            val grammar =
    //                    "${MethodSelectorNode.relationalToken}$methodName${PropertyBlockSelectorNode.startToken}selectorName${PropertyBlockSelectorNode.endToken}"
    //            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)
    //
    //            TestUtils.processAndCheckEmpty(analyzer)
    //        }
    //    }

    @Test
    @Incorrect
    fun `test incorrect unrecognized method name`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.UnrecognizedSelectorMethod) {
            val methodName = "methodName"
            val grammar = "${MethodSelectorNode.relationalToken}$methodName"
            val analyzer = TestUtils.createAnalyzerFrom(grammar, parserFunction = MethodSelectorNode.Companion::parse)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
