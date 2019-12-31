package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class VarDeclarationStmtAnalyzerTest {
    @Test
    fun `test simple normal`() {
        val varName = "test"
        val text =
                "${VarDeclarationStmtNode.variableKeyword} $varName ${VarDeclarationStmtNode.assignOperator} ${LxmLogic.True}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarDeclarationStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val property = context.getPropertyDescriptor(varName) ?: throw Error("The property cannot be null")
        val value = property.value.dereference(analyzer.memory, toWrite = false) as? LxmLogic ?: throw Error(
                "The result must be a LxmLogic")

        Assertions.assertFalse(property.isConstant, "The primitive property is incorrect")
        Assertions.assertTrue(value.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test simple constant`() {
        val varName = "test"
        val text =
                "${VarDeclarationStmtNode.constKeyword} $varName ${VarDeclarationStmtNode.assignOperator} ${LxmLogic.True}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarDeclarationStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val property = context.getPropertyDescriptor(varName) ?: throw Error("The property cannot be null")
        val value = property.value.dereference(analyzer.memory, toWrite = false) as? LxmLogic ?: throw Error(
                "The result must be a LxmLogic")

        Assertions.assertTrue(property.isConstant, "The primitive property is incorrect")
        Assertions.assertTrue(value.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test destructuring object`() {
        val elementAlias = "elementAlias"
        val valueInt = 5
        val objectValue =
                "${ObjectNode.startToken} $elementAlias ${ObjectElementNode.keyValueSeparator} $valueInt ${ObjectNode.endToken}"
        val destructuring = "${DestructuringStmtNode.startToken} $elementAlias ${DestructuringStmtNode.endToken}"
        val text =
                "${VarDeclarationStmtNode.variableKeyword} $destructuring ${VarDeclarationStmtNode.assignOperator} $objectValue"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarDeclarationStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val variable = context.getDereferencedProperty<LxmInteger>(elementAlias, toWrite = false) ?: throw Error(
                "The variable must be a LxmInteger")

        Assertions.assertEquals(valueInt, variable.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(elementAlias))
    }

    @Test
    fun `test destructuring list`() {
        val elementAlias = "elementAlias"
        val valueInt = 5
        val listValue = "${ListNode.startToken} $valueInt ${ListNode.endToken}"
        val destructuring = "${DestructuringStmtNode.startToken} $elementAlias ${DestructuringStmtNode.endToken}"
        val text =
                "${VarDeclarationStmtNode.variableKeyword} $destructuring ${VarDeclarationStmtNode.assignOperator} $listValue"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarDeclarationStmtNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val variable = context.getDereferencedProperty<LxmInteger>(elementAlias, toWrite = false) ?: throw Error(
                "The variable must be a LxmInteger")

        Assertions.assertEquals(valueInt, variable.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(elementAlias))
    }

    @Test
    @Incorrect
    fun `test incorrect destructuring`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val elementAlias = "elementAlias"
            val destructuring = "${DestructuringStmtNode.startToken} $elementAlias ${DestructuringStmtNode.endToken}"
            val text =
                    "${VarDeclarationStmtNode.variableKeyword} $destructuring ${VarDeclarationStmtNode.assignOperator} ${LxmLogic.True}"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarDeclarationStmtNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `test incorrect identifier`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType) {
            val text =
                    "${VarDeclarationStmtNode.constKeyword} ${EscapedExpressionNode.startToken}${LxmLogic.True}${EscapedExpressionNode.endToken} ${VarDeclarationStmtNode.assignOperator} ${LxmLogic.True}"
            val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = VarDeclarationStmtNode.Companion::parse)
            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
