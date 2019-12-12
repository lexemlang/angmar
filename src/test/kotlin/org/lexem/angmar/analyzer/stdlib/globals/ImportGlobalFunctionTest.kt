package org.lexem.angmar.analyzer.stdlib.globals

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class ImportGlobalFunctionTest {
    @Test
    fun `import a relative file`() {
        val varName = "test"
        val importedFile = "imported"
        val text =
                "import(${StringNode.startToken}$importedFile${StringNode.endToken}) \n $varName ${MultiplicativeExpressionNode.multiplicationOperator}${AssignOperatorNode.assignOperator} 2"
        val textImported = "$varName ${AssignOperatorNode.assignOperator} 1"

        TestUtils.handleTempFiles(mapOf("main" to text, importedFile to textImported)) { files ->
            val parser = LexemParser(IOStringReader.from(files["main"]!!))
            val grammar = LexemFileNode.parse(parser)

            Assertions.assertNotNull(grammar, "The grammar cannot be null")

            val analyzer = LexemAnalyzer(grammar!!)

            // Prepare context.
            val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
            initialContext.setProperty(analyzer.memory, varName, LxmInteger.Num0)

            TestUtils.processAndCheckEmpty(analyzer)

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val result = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(2, result.primitive, "The primitive property is incorrect")

            TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
        }
    }

    @Test
    fun `import an absolute file`() {
        val varName = "test"
        val importedFile = "imported"
        val textImported = "$varName ${AssignOperatorNode.assignOperator} 1"

        TestUtils.handleTempFiles(mapOf("main" to "", importedFile to textImported)) { files ->
            // Rewrite main.
            val text =
                    "import(${StringNode.startToken}${files[importedFile]!!.canonicalPath}${StringNode.endToken}) \n $varName ${MultiplicativeExpressionNode.multiplicationOperator}${AssignOperatorNode.assignOperator} 2"
            files["main"]!!.writeText(text)

            val parser = LexemParser(IOStringReader.from(files["main"]!!))
            val grammar = LexemFileNode.parse(parser)

            Assertions.assertNotNull(grammar, "The grammar cannot be null")

            val analyzer = LexemAnalyzer(grammar!!)

            // Prepare context.
            val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
            initialContext.setProperty(analyzer.memory, varName, LxmInteger.Num0)

            TestUtils.processAndCheckEmpty(analyzer)

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val result = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(2, result.primitive, "The primitive property is incorrect")

            TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
        }
    }

    @Test
    fun `import a file from root`() {
        val varName = "test"
        val importedFile = "imported"
        val text =
                "import(${StringNode.startToken}root:$importedFile${StringNode.endToken}) \n $varName ${MultiplicativeExpressionNode.multiplicationOperator}${AssignOperatorNode.assignOperator} 2"
        val textImported = "$varName ${AssignOperatorNode.assignOperator} 1"

        TestUtils.handleTempFiles(mapOf("main" to text, importedFile to textImported)) { files ->
            val parser = LexemParser(IOStringReader.from(files["main"]!!))
            val grammar = LexemFileNode.parse(parser)

            Assertions.assertNotNull(grammar, "The grammar cannot be null")

            val analyzer = LexemAnalyzer(grammar!!)

            // Prepare context.
            val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
            initialContext.setProperty(analyzer.memory, varName, LxmInteger.Num0)

            TestUtils.processAndCheckEmpty(analyzer)

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val result = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(2, result.primitive, "The primitive property is incorrect")

            TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
        }
    }

    @Test
    fun `import an http file`() {
        val varName = "test"
        val importedFile =
                "https://raw.githubusercontent.com/lexemlang/angmar/master/src/test/resources/remoteTestFile.lxm"
        val text = "import(${StringNode.startToken}$importedFile${StringNode.endToken})"

        TestUtils.handleTempFiles(mapOf("main" to text)) { files ->
            val parser = LexemParser(IOStringReader.from(files["main"]!!))
            val grammar = LexemFileNode.parse(parser)

            Assertions.assertNotNull(grammar, "The grammar cannot be null")

            val analyzer = LexemAnalyzer(grammar!!)

            // Prepare context.
            val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
            initialContext.setProperty(analyzer.memory, varName, LxmInteger.Num0)

            TestUtils.processAndCheckEmpty(analyzer)

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val result = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(1, result.primitive, "The primitive property is incorrect")

            TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
        }
    }

    @Test
    fun `import twice the same file`() {
        val varName = "test"
        val importedFile = "imported"
        val text =
                "import(${StringNode.startToken}$importedFile${StringNode.endToken}) \n import(${StringNode.startToken}$importedFile${StringNode.endToken})"
        val textImported = "$varName ${AdditiveExpressionNode.additionOperator}${AssignOperatorNode.assignOperator} 1"

        TestUtils.handleTempFiles(mapOf("main" to text, importedFile to textImported)) { files ->
            val parser = LexemParser(IOStringReader.from(files["main"]!!))
            val grammar = LexemFileNode.parse(parser)

            Assertions.assertNotNull(grammar, "The grammar cannot be null")

            val analyzer = LexemAnalyzer(grammar!!)

            // Prepare context.
            val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
            initialContext.setProperty(analyzer.memory, varName, LxmInteger.Num0)

            TestUtils.processAndCheckEmpty(analyzer)

            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val result = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                    "The result must be a LxmInteger")
            Assertions.assertEquals(1, result.primitive, "The primitive property is incorrect")

            TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
        }
    }

    @Test
    fun `import in allIn mode`() {
        val varName = "test"
        val fileName = "main"
        val fileNameImported = "imported"
        val text =
                "import(${StringNode.startToken}$fileNameImported${StringNode.endToken}) \n $varName ${MultiplicativeExpressionNode.multiplicationOperator}${AssignOperatorNode.assignOperator} 2"
        val textImported = "$varName ${AssignOperatorNode.assignOperator} 1"

        val parserMain = LexemParser(IOStringReader.from(text))
        val parserImported = LexemParser(IOStringReader.from(textImported))

        val analyzer =
                LexemAnalyzer.createFrom(mapOf(fileName to parserMain, fileNameImported to parserImported), fileName)
                        ?: throw Error("The analyzer cannot be null")

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val result = context.getPropertyValue(analyzer.memory, varName) as? LxmInteger ?: throw Error(
                "The result must be a LxmInteger")
        Assertions.assertEquals(2, result.primitive, "The primitive property is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    @Incorrect
    fun `import without uri`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError) {
            val text = "import()"
            val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `import an undefined file`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist) {
            val text = "import(${StringNode.startToken}/lexem-file-not-found${StringNode.endToken})"
            val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `import a no Lexem file`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.FileIsNotLexem) {
            val importedFile = "imported"
            val text = "import(${StringNode.startToken}$importedFile${StringNode.endToken})"
            val textImported = ")"

            TestUtils.handleTempFiles(mapOf("main" to text, importedFile to textImported)) { files ->
                val parser = LexemParser(IOStringReader.from(files["main"]!!))
                val grammar = LexemFileNode.parse(parser)

                Assertions.assertNotNull(grammar, "The grammar cannot be null")

                val analyzer = LexemAnalyzer(grammar!!)
                TestUtils.processAndCheckEmpty(analyzer)
                TestUtils.checkEmptyStackAndContext(analyzer)
            }
        }
    }

    @Test
    @Incorrect
    fun `import an incorrect http file`() {
        val importedFile = "https://bad.url.to.test/"
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist) {
            val text = "import(${StringNode.startToken}$importedFile${StringNode.endToken})"
            val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }

    @Test
    @Incorrect
    fun `import an incorrect uri`() {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist) {
            val text = "import(${StringNode.startToken}_$%<>!${StringNode.endToken})"
            val analyzer = TestUtils.createAnalyzerFrom(text) { parser, _, _ ->
                LexemFileNode.parse(parser)
            }

            TestUtils.processAndCheckEmpty(analyzer)
            TestUtils.checkEmptyStackAndContext(analyzer)
        }
    }

    @ParameterizedTest
    @Incorrect
    @ValueSource(
            strings = ["a", "/x/y", "https://raw.githubusercontent.com/lexemlang/angmar/master/src/test/resources/remoteTestFile.lxm"])
    fun `import an incorrect uri in allIn mode`(url: String) {
        TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.FileNotExist) {
            val fileName = "main"
            val text = "import(${StringNode.startToken}$url${StringNode.endToken})"

            val parserMain = LexemParser(IOStringReader.from(text))

            val analyzer = LexemAnalyzer.createFrom(mapOf(fileName to parserMain), fileName) ?: throw Error(
                    "The analyzer cannot be null")

            TestUtils.processAndCheckEmpty(analyzer)
        }
    }
}
