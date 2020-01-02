package org.lexem.angmar

import org.junit.jupiter.api.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.literals.*
import org.lexem.angmar.utils.*

internal class LexemAnalyzerTest {
    @Test
    fun `test error stacktrace 1`() {
        val importedFile = "imported"
        val text = """
                   let importFunction = import("$importedFile")
                   
                   fun errorThrown(message) {
                       importFunction.errorThrownImport1(message)
                   }

                   errorThrown("This is a test")
               """.trimIndent()
        val textImported = """
                   filter errorThrownImport2(message) {
                       (fun(message) {
                           Debug.throw("Error thrown with message: " + message)
                       })(message)
                   }

                   pub! exp errorThrownImport1(message) {
                       errorThrownImport2(message, node2Filter: node)
                   }
               """.trimIndent()

        TestUtils.handleTempFiles(mapOf("main" to text, importedFile to textImported)) { files ->
            TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CustomError) {
                val analyzer = TestUtils.createAnalyzerFromFile(files["main"]!!)
                TestUtils.processAndCheckEmpty(analyzer)
            }
        }
    }

    @Test
    fun `test error stacktrace 2`() {
        val importedFile = "imported"
        val text = """
                   let importFunction = import("$importedFile")
               """.trimIndent()
        val textImported = """
                   Debug.throw("This is a test")
               """.trimIndent()

        TestUtils.handleTempFiles(mapOf("main" to text, importedFile to textImported)) { files ->
            TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CustomError) {
                val analyzer = TestUtils.createAnalyzerFromFile(files["main"]!!)
                TestUtils.processAndCheckEmpty(analyzer)
            }
        }
    }

    @Test
    fun `test error stacktrace 3`() {
        val text = """
                   let prototype = {
                       toString() {
                           Debug.throw("This is a test")
                       }
                   }
                   
                   let obj = Object.newFrom(prototype)
                   
                   let result = "x" + obj
               """.trimIndent()

        TestUtils.handleTempFiles(mapOf("main" to text)) { files ->
            TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CustomError) {
                val analyzer = TestUtils.createAnalyzerFromWholeGrammar(files["main"]!!.readText())
                TestUtils.processAndCheckEmpty(analyzer)
            }
        }
    }

    @Test
    fun `test entryPoint - missing`() {
        val varName = "test"
        val text = "$varName ${AssignOperatorNode.assignOperator} ${AnalyzerCommons.Identifiers.EntryPoint}"
        val analyzer = TestUtils.createAnalyzerFromWholeGrammar(text)

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, varName, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val result = context.getPropertyValue(analyzer.memory, varName) as? LxmString ?: throw Error(
                "The result must be a LxmString")
        Assertions.assertEquals(Consts.defaultEntryPoint, result.primitive, "The result is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName))
    }

    @Test
    fun `test entryPoint access from different files`() {
        val varName1 = "test1"
        val varName2 = "test2"
        val fileName = "main"
        val fileNameImported = "imported"
        val entryPoint = "entryPointTest"
        val text =
                "import(${StringNode.startToken}$fileNameImported${StringNode.endToken}) \n $varName1 ${AssignOperatorNode.assignOperator} ${AnalyzerCommons.Identifiers.EntryPoint}"
        val textImported = "$varName2 ${AssignOperatorNode.assignOperator} ${AnalyzerCommons.Identifiers.EntryPoint}"

        val mainReader = IOStringReader.from(text)
        val importedReader = IOStringReader.from(textImported)

        val analyzer =
                LexemAnalyzer.createFrom(mapOf(fileName to mainReader, fileNameImported to importedReader), fileName)
                        ?: throw Error("The analyzer cannot be null")

        // Prepare context.
        val initialContext = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
        initialContext.setProperty(analyzer.memory, varName1, LxmInteger.Num0)
        initialContext.setProperty(analyzer.memory, varName2, LxmInteger.Num0)

        TestUtils.processAndCheckEmpty(analyzer, entryPoint = entryPoint)

        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val result1 = context.getPropertyValue(analyzer.memory, varName1) as? LxmString ?: throw Error(
                "The result1 must be a LxmString")
        val result2 = context.getPropertyValue(analyzer.memory, varName2)
        Assertions.assertEquals(entryPoint, result1.primitive, "The result1 is incorrect")
        Assertions.assertEquals(LxmNil, result2, "The result2 is incorrect")

        TestUtils.checkEmptyStackAndContext(analyzer, listOf(varName1, varName2))
    }
}
