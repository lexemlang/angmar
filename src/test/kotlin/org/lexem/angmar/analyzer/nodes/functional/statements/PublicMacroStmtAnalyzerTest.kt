package org.lexem.angmar.analyzer.nodes.functional.statements

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class PublicMacroStmtAnalyzerTest {
    @Test
    fun `test function`() {
        val fnName = "fn"
        val text =
                "${PublicMacroStmtNode.macroName} ${FunctionStmtNode.keyword} $fnName ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PublicMacroStmtNode.Companion::parse)

        // Prepare context
        val obj = LxmObject(analyzer.memory)
        val objRef = analyzer.memory.add(obj)
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Exports, objRef, isConstant = true)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.Exports,
                toWrite = false)!!
        val function =
                exports.getPropertyDescriptor(analyzer.memory, fnName) ?: throw Error("The function has not been set")

        Assertions.assertFalse(function.isConstant, "The isConstant property is incorrect")
        Assertions.assertTrue(function.value.dereference(analyzer.memory, toWrite = false) is LxmFunction,
                "The value property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(fnName, AnalyzerCommons.Identifiers.Exports))
    }

    @Test
    fun `test simple normal`() {
        val varName = "test"
        val text =
                "${PublicMacroStmtNode.macroName} ${VarDeclarationStmtNode.variableKeyword} $varName ${VarDeclarationStmtNode.assignOperator} ${LxmLogic.True}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PublicMacroStmtNode.Companion::parse)

        // Prepare context
        val obj = LxmObject(analyzer.memory)
        val objRef = analyzer.memory.add(obj)
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Exports, objRef, isConstant = true)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.Exports,
                toWrite = false)!!
        val variable =
                exports.getPropertyDescriptor(analyzer.memory, varName) ?: throw Error("The variable has not been set")

        Assertions.assertFalse(variable.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(LxmLogic.True, variable.value, "The value property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName, AnalyzerCommons.Identifiers.Exports))
    }

    @Test
    fun `test simple constant`() {
        val varName = "test"
        val text =
                "${PublicMacroStmtNode.macroName} ${VarDeclarationStmtNode.constKeyword} $varName ${VarDeclarationStmtNode.assignOperator} ${LxmLogic.True}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PublicMacroStmtNode.Companion::parse)

        // Prepare context
        val obj = LxmObject(analyzer.memory)
        val objRef = analyzer.memory.add(obj)
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Exports, objRef, isConstant = true)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.Exports,
                toWrite = false)!!
        val variable =
                exports.getPropertyDescriptor(analyzer.memory, varName) ?: throw Error("The variable has not been set")

        Assertions.assertTrue(variable.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(LxmLogic.True, variable.value, "The value property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName, AnalyzerCommons.Identifiers.Exports))
    }

    @Test
    fun `test destructuring`() {
        val elementAlias = "elementAlias"
        val valueInt = LxmInteger.Num10
        val objectValue =
                "${ObjectNode.startToken} $elementAlias ${ObjectElementNode.keyValueSeparator} $valueInt ${ObjectNode.endToken}"
        val destructuring = "${DestructuringStmtNode.startToken} $elementAlias ${DestructuringStmtNode.endToken}"
        val text =
                "${PublicMacroStmtNode.macroName} ${VarDeclarationStmtNode.variableKeyword} $destructuring ${VarDeclarationStmtNode.assignOperator} $objectValue"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PublicMacroStmtNode.Companion::parse)

        // Prepare context
        val obj = LxmObject(analyzer.memory)
        val objRef = analyzer.memory.add(obj)
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Exports, objRef, isConstant = true)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.Exports,
                toWrite = false)!!
        val variable = exports.getPropertyDescriptor(analyzer.memory, elementAlias) ?: throw Error(
                "The variable has not been set")

        Assertions.assertFalse(variable.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(valueInt, variable.value, "The value property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(elementAlias, AnalyzerCommons.Identifiers.Exports))
    }

    @Test
    fun `test constant destructuring`() {
        val elementAlias = "elementAlias"
        val valueInt = LxmInteger.Num10
        val objectValue =
                "${ObjectNode.startToken} $elementAlias ${ObjectElementNode.keyValueSeparator} $valueInt ${ObjectNode.endToken}"
        val destructuring = "${DestructuringStmtNode.startToken} $elementAlias ${DestructuringStmtNode.endToken}"
        val text =
                "${PublicMacroStmtNode.macroName} ${VarDeclarationStmtNode.constKeyword} $destructuring ${VarDeclarationStmtNode.assignOperator} $objectValue"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PublicMacroStmtNode.Companion::parse)

        // Prepare context
        val obj = LxmObject(analyzer.memory)
        val objRef = analyzer.memory.add(obj)
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Exports, objRef, isConstant = true)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.Exports,
                toWrite = false)!!
        val variable = exports.getPropertyDescriptor(analyzer.memory, elementAlias) ?: throw Error(
                "The variable has not been set")

        Assertions.assertTrue(variable.isConstant, "The isConstant property is incorrect")
        Assertions.assertEquals(valueInt, variable.value, "The value property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(elementAlias, AnalyzerCommons.Identifiers.Exports))
    }

    @Test
    fun `test expression`() {
        val expName = "expr"
        val text =
                "${PublicMacroStmtNode.macroName} ${ExpressionStmtNode.keyword} $expName ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PublicMacroStmtNode.Companion::parse)

        // Prepare context
        val obj = LxmObject(analyzer.memory)
        val objRef = analyzer.memory.add(obj)
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Exports, objRef, isConstant = true)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.Exports,
                toWrite = false)!!
        val function =
                exports.getPropertyDescriptor(analyzer.memory, expName) ?: throw Error("The function has not been set")

        Assertions.assertFalse(function.isConstant, "The isConstant property is incorrect")
        Assertions.assertTrue(function.value.dereference(analyzer.memory, toWrite = false) is LxmFunction,
                "The value property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(expName, AnalyzerCommons.Identifiers.Exports))
    }

    @Test
    fun `test filter`() {
        val filterName = "filter"
        val text =
                "${PublicMacroStmtNode.macroName} ${ExpressionStmtNode.keyword} $filterName ${BlockStmtNode.startToken}${BlockStmtNode.endToken}"
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = PublicMacroStmtNode.Companion::parse)

        // Prepare context
        val obj = LxmObject(analyzer.memory)
        val objRef = analyzer.memory.add(obj)
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Exports, objRef, isConstant = true)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory, AnalyzerCommons.Identifiers.Exports,
                toWrite = false)!!
        val function = exports.getPropertyDescriptor(analyzer.memory, filterName) ?: throw Error(
                "The function has not been set")

        Assertions.assertFalse(function.isConstant, "The isConstant property is incorrect")
        Assertions.assertTrue(function.value.dereference(analyzer.memory, toWrite = false) is LxmFunction,
                "The value property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(filterName, AnalyzerCommons.Identifiers.Exports))
    }
}
