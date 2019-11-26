package org.lexem.angmar.analyzer.nodes.functional.expressions.macros

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.utils.*

internal class MacroExpressionAnalyzerTest {
    @Test
    fun `test lineMacro`() {
        val text = "5 ${MultiplicativeExpressionNode.multiplicationOperator}\n ${MacroExpressionNode.lineMacro}"
        val analyzer =
                TestUtils.createAnalyzerFrom(text, parserFunction = MultiplicativeExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw Error("The result must be a LxmInteger")
        Assertions.assertEquals(10, result.primitive, "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }

    @Test
    fun `test fileMacro`() {
        val text = MacroExpressionNode.fileMacro

        TestUtils.handleTempFiles(mapOf("main" to text)) { files ->
            val mainPath = files["main"]!!.canonicalPath
            val analyzer = TestUtils.createAnalyzerFromFile(mainPath, MacroExpressionNode.Companion::parse)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenFilePath, LxmString.from(mainPath))

            TestUtils.processAndCheckEmpty(analyzer)

            val result =
                    analyzer.memory.getLastFromStack() as? LxmString ?: throw Error("The result must be a LxmString")
            Assertions.assertEquals(mainPath, result.primitive, "The value inserted in the stack is incorrect")

            // Remove Last from the stack.
            analyzer.memory.removeLastFromStack()

            TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenFilePath))
        }
    }

    @Test
    fun `test directoryMacro`() {
        val text = MacroExpressionNode.directoryMacro

        TestUtils.handleTempFiles(mapOf("main" to text)) { files ->
            val mainPath = files["main"]!!.canonicalPath
            val analyzer = TestUtils.createAnalyzerFromFile(mainPath, MacroExpressionNode.Companion::parse)

            // Prepare context.
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenFilePath, LxmString.from(mainPath))

            TestUtils.processAndCheckEmpty(analyzer)

            val result =
                    analyzer.memory.getLastFromStack() as? LxmString ?: throw Error("The result must be a LxmString")
            Assertions.assertEquals(files["main"]!!.parentFile.canonicalPath, result.primitive,
                    "The value inserted in the stack is incorrect")

            // Remove Last from the stack.
            analyzer.memory.removeLastFromStack()

            TestUtils.checkEmptyStackAndContext(analyzer, listOf(AnalyzerCommons.Identifiers.HiddenFilePath))
        }
    }

    @Test
    fun `test fileContentMacro`() {
        val text = MacroExpressionNode.fileContentMacro
        val analyzer = TestUtils.createAnalyzerFrom(text, parserFunction = MacroExpressionNode.Companion::parse)
        TestUtils.processAndCheckEmpty(analyzer)

        val result = analyzer.memory.getLastFromStack() as? LxmString ?: throw Error("The result must be a LxmString")
        Assertions.assertEquals(MacroExpressionNode.fileContentMacro, result.primitive,
                "The value inserted in the stack is incorrect")

        // Remove Last from the stack.
        analyzer.memory.removeLastFromStack()

        TestUtils.checkEmptyStackAndContext(analyzer)
    }
}
