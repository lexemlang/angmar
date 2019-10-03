package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.selective.*
import org.lexem.angmar.utils.*

internal class VarPatternSelectiveStmtAnalyzerTest {
    @Test
    fun `test simple normal`() {
        val varName = "test"
        val text = "${VarPatternSelectiveStmtNode.variableKeyword} $varName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarPatternSelectiveStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmLogic.True)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property =
                context.getOwnPropertyDescriptor(analyzer.memory, varName) ?: throw Error("The property cannot be null")
        val value =
                property.value.dereference(analyzer.memory) as? LxmLogic ?: throw Error("The result must be a LxmLogic")

        Assertions.assertFalse(property.isConstant, "The primitive property is incorrect")
        Assertions.assertTrue(value.primitive, "The primitive property is incorrect")

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(), "The mainValue is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test simple constant`() {
        val varName = "test"
        val text = "${VarPatternSelectiveStmtNode.constKeyword} $varName"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarPatternSelectiveStmtNode.Companion::parse)

        // Prepare stack.
        analyzer.memory.pushStack(LxmLogic.True)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val property =
                context.getOwnPropertyDescriptor(analyzer.memory, varName) ?: throw Error("The property cannot be null")
        val value =
                property.value.dereference(analyzer.memory) as? LxmLogic ?: throw Error("The result must be a LxmLogic")

        Assertions.assertTrue(property.isConstant, "The primitive property is incorrect")
        Assertions.assertTrue(value.primitive, "The primitive property is incorrect")

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(), "The mainValue is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test destructuring object`() {
        val elementAlias = "elementAlias"
        val valueInt = 5
        val destructuring = "${DestructuringStmtNode.startToken} $elementAlias ${DestructuringStmtNode.endToken}"
        val text = "${VarPatternSelectiveStmtNode.variableKeyword} $destructuring"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarPatternSelectiveStmtNode.Companion::parse)

        // Prepare stack.
        val obj = LxmObject()
        val objRef = analyzer.memory.add(obj)
        obj.setProperty(analyzer.memory, elementAlias, LxmInteger.from(valueInt))

        analyzer.memory.pushStack(objRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val variable = context.getDereferencedProperty<LxmInteger>(analyzer.memory, elementAlias) ?: throw Error(
                "The variable must be a LxmInteger")

        Assertions.assertEquals(valueInt, variable.primitive, "The primitive property is incorrect")

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(), "The mainValue is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(elementAlias))
    }

    @Test
    fun `test destructuring list`() {
        val elementAlias = "elementAlias"
        val valueInt = 5
        val destructuring = "${DestructuringStmtNode.startToken} $elementAlias ${DestructuringStmtNode.endToken}"
        val text = "${VarPatternSelectiveStmtNode.variableKeyword} $destructuring"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarPatternSelectiveStmtNode.Companion::parse)

        // Prepare stack.
        val list = LxmList()
        val listRef = analyzer.memory.add(list)
        list.addCell(analyzer.memory, LxmInteger.from(valueInt))

        analyzer.memory.pushStack(listRef)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val variable = context.getDereferencedProperty<LxmInteger>(analyzer.memory, elementAlias) ?: throw Error(
                "The variable must be a LxmInteger")

        Assertions.assertEquals(valueInt, variable.primitive, "The primitive property is incorrect")

        Assertions.assertEquals(LxmLogic.True, analyzer.memory.popStack(), "The mainValue is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(elementAlias))
    }

    @Test
    @Incorrect
    fun `test incorrect destructuring`() {
        TestUtils.assertAnalyzerException {
            val elementAlias = "elementAlias"
            val destructuring = "${DestructuringStmtNode.startToken} $elementAlias ${DestructuringStmtNode.endToken}"
            val text = "${VarPatternSelectiveStmtNode.variableKeyword} $destructuring"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = VarPatternSelectiveStmtNode.Companion::parse)

            // Prepare stack.
            analyzer.memory.pushStack(LxmInteger.Num10)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect identifier`() {
        TestUtils.assertAnalyzerException {
            val text =
                    "${VarPatternSelectiveStmtNode.constKeyword} ${EscapedExpressionNode.startToken}${LxmLogic.True}${EscapedExpressionNode.endToken}"
            val analyzer =
                    TestUtils.createAnalyzerFrom(text, parserFunction = VarPatternSelectiveStmtNode.Companion::parse)

            // Prepare stack.
            analyzer.memory.pushStack(LxmInteger.Num10)

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
