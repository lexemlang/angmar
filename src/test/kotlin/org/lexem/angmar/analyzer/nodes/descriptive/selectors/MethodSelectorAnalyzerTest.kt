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
        val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
        if (!isOk) {
            val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
            lxmNode.addToParent(analyzer.memory, lxmParent)
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
        } else {
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmParent)
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(isOk), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        if (!isOk) {
            lxmParent.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(
                    analyzer.memory, AnalyzerCommons.Identifiers.Children, LxmNil, ignoreConstant = true)
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
        val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        if (!isOk) {
            val lxmChild = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
            lxmNode.getChildren(analyzer.memory, toWrite = true).addCell(lxmChild, ignoreConstant = true)
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
        val lxmParent = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                null
            }
            // Incorrect - no first
            1 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNodeAux = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNodeAux.addToParent(analyzer.memory, lxmParent)
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
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
            lxmParent!!.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(
                    analyzer.memory, AnalyzerCommons.Identifiers.Children, LxmNil, ignoreConstant = true)
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
        val lxmParent = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                null
            }
            // Incorrect - no last
            1 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNodeAux = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)
                lxmNodeAux.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
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
            lxmParent!!.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(
                    analyzer.memory, AnalyzerCommons.Identifiers.Children, LxmNil, ignoreConstant = true)
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
            val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
            val lxmNodeAux = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
            lxmNode.getChildren(analyzer.memory, toWrite = true).addCell(lxmNodeAux, ignoreConstant = true)

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
        } else {
            val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
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
        val lxmParent = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                null
            }
            // Incorrect - no at 1
            1 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNodeAux = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNodeAux.addToParent(analyzer.memory, lxmParent)
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
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
            lxmParent!!.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(
                    analyzer.memory, AnalyzerCommons.Identifiers.Children, LxmNil, ignoreConstant = true)
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
        val lxmParent = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

                null
            }
            // Incorrect - no same name
            1 -> {
                val lxmParent = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

                lxmParent
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

                lxmParent
            }
            else -> null
        }

        TestUtils.processAndCheckEmpty(analyzer)

        Assertions.assertEquals(LxmLogic.from(type == 2), analyzer.memory.getLastFromStack(), "The result is incorrect")

        // Remove Node and Last from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
        analyzer.memory.removeLastFromStack()

        if (type != 0) {
            lxmParent!!.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(
                    analyzer.memory, AnalyzerCommons.Identifiers.Children, LxmNil, ignoreConstant = true)
        }

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
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Correct - all with the name
            1 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Incorrect - one without the name
            2 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
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
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Incorrect - no one with the name
            1 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Correct - one with name
            2 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
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
            val lxmNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
        } else {
            val lxmNode = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())

            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
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
        val lxmParent = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                null
            }
            // Incorrect - parent with different name
            1 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
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
            lxmParent!!.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(
                    analyzer.memory, AnalyzerCommons.Identifiers.Children, LxmNil, ignoreConstant = true)
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
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Correct - all with the name
            1 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Incorrect - one without the name
            2 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
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
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Incorrect - no one with the name
            1 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Correct - one with name
            2 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
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
            val lxmNode = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
        } else {
            val lxmNode = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
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
        val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)

        if (isOk) {
            val lxmChild = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
            lxmNode.getChildren(analyzer.memory, toWrite = true).addCell(lxmChild, ignoreConstant = true)
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
        val lxmParent = when (type) {
            // Incorrect - no parent
            0 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                null
            }
            // Incorrect - no first
            1 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNodeAux = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNodeAux.addToParent(analyzer.memory, lxmParent)
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
            }
            // Correct
            2 -> {
                val lxmParent = LxmNode(analyzer.memory, "root", analyzer.text.saveCursor())
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                lxmNode.addToParent(analyzer.memory, lxmParent)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
                lxmParent
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
            lxmParent!!.getPrimitive().dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!.setProperty(
                    analyzer.memory, AnalyzerCommons.Identifiers.Children, LxmNil, ignoreConstant = true)
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
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Correct - all with the name
            1 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
            }
            // Incorrect - one without the name
            2 -> {
                val lxmNode = LxmNode(analyzer.memory, "nodeName", analyzer.text.saveCursor())
                val lxmAux1 = LxmNode(analyzer.memory, nodeName, analyzer.text.saveCursor())
                val lxmAux2 = LxmNode(analyzer.memory, nodeName + "x", analyzer.text.saveCursor())
                val children = lxmNode.getChildren(analyzer.memory, toWrite = true)
                children.addCell(lxmAux1, ignoreConstant = true)
                children.addCell(lxmAux2, ignoreConstant = true)

                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNode)
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
