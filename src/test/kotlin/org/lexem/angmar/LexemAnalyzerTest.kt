package org.lexem.angmar

import org.junit.jupiter.api.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*

internal class LexemAnalyzerTest {
    @Test
    fun `error stack test 1`() {
        val importedFile = "imported"
        val text = """
                   let importFunction = import("$importedFile")
                   
                   fun errorThrown(message) {
                       importFunction.errorThrownImport1(message)
                   }

                   errorThrown("This is a test")
               """.trimIndent()
        val textImported = """
                   fun errorThrownImport2(message) {
                       (fun(message) {
                           Debug.throw("Error thrown with message: " + message)
                       })(message)
                   }

                   pub! fun errorThrownImport1(message) {
                       errorThrownImport2(message)
                   }
               """.trimIndent()

        TestUtils.handleTempFiles(mapOf("main" to text, importedFile to textImported)) { files ->
            TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CustomError) {
                val parser = LexemParser(IOStringReader.from(files["main"]!!))
                val grammar = LexemFileNode.parse(parser)

                Assertions.assertNotNull(grammar, "The grammar cannot be null")

                val analyzer = LexemAnalyzer(grammar!!)
                TestUtils.processAndCheckEmpty(analyzer)
            }
        }
    }

    @Test
    fun `error stack test 2`() {
        val importedFile = "imported"
        val text = """
                   let importFunction = import("$importedFile")
               """.trimIndent()
        val textImported = """
                   Debug.throw("This is a test")
               """.trimIndent()

        TestUtils.handleTempFiles(mapOf("main" to text, importedFile to textImported)) { files ->
            TestUtils.assertAnalyzerException(AngmarAnalyzerExceptionType.CustomError) {
                val parser = LexemParser(IOStringReader.from(files["main"]!!))
                val grammar = LexemFileNode.parse(parser)

                Assertions.assertNotNull(grammar, "The grammar cannot be null")

                val analyzer = LexemAnalyzer(grammar!!)
                TestUtils.processAndCheckEmpty(analyzer)
            }
        }
    }

    @Test
    fun `error stack test 3`() {
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
                val parser = LexemParser(IOStringReader.from(files["main"]!!))
                val grammar = LexemFileNode.parse(parser)

                Assertions.assertNotNull(grammar, "The grammar cannot be null")

                val analyzer = LexemAnalyzer(grammar!!)
                TestUtils.processAndCheckEmpty(analyzer)
            }
        }
    }
}
