package org.lexem.angmar.commands

import com.ginsberg.junit.exit.*
import org.junit.jupiter.api.*
import org.lexem.angmar.config.*
import org.lexem.angmar.utils.*
import kotlin.system.*

internal class AngmarCommandTest {
    @Test
    @ExpectSystemExit
    fun `version option`() {
        TestUtils.callAngmar(arrayOf("--version")) { stdOut, errOut ->
            println(stdOut)
            Assertions.assertTrue(errOut.isEmpty(), "The error output must be empty")
            Assertions.assertTrue(stdOut.contains(Consts.Commands.parser))
            Assertions.assertTrue(stdOut.contains(Consts.projectVersion))
        }

        TestUtils.callAngmar(arrayOf("-v")) { stdOut, errOut ->
            println(stdOut)
            Assertions.assertTrue(errOut.isEmpty(), "The error output must be empty")
            Assertions.assertTrue(stdOut.contains(Consts.Commands.parser))
            Assertions.assertTrue(stdOut.contains(Consts.projectVersion))
        }
    }

    @Test
    @ExpectSystemExit
    fun `no source`() {
        TestUtils.callAngmar(arrayOf()) { stdOut, errOut ->
            println(errOut)
            Assertions.assertTrue(stdOut.isEmpty(), "The output must be empty")
            Assertions.assertTrue(errOut.startsWith("Usage:"))
            Assertions.assertTrue(errOut.contains("Error: Missing argument '${AngmarCommand.grammarSourceArgument}'."))
        }
    }

    @Test
    @ExpectSystemExit
    fun `no texts to analyze`() {
        TestUtils.callAngmar(arrayOf("file")) { stdOut, errOut ->
            println(errOut)
            Assertions.assertTrue(stdOut.isEmpty(), "The output must be empty")
            Assertions.assertTrue(errOut.startsWith("Usage:"))
            Assertions.assertTrue(errOut.contains("Error: Missing argument '${AngmarCommand.textsArgument}'."))
        }
    }

    @Test
    @ExpectSystemExit
    fun `2 files test`() {
        val importedFileName = "importedFile"
        val text =
                "let varName = 1\n Globals.value = varName \n import(\"./$importedFileName\") \n Globals.value = Globals.value + \"-suffix\""
        val textImported = "Globals.value = \"str-value\""

        TestUtils.handleTempFiles(mapOf("main" to text, importedFileName to textImported, "text" to "")) {
            val mainUrl = it["main"]!!
            val textUrl = it["text"]!!
            TestUtils.callAngmar(arrayOf(mainUrl.canonicalPath, textUrl.canonicalPath)) { stdOut, errOut ->
                println(stdOut)
                Assertions.assertTrue(errOut.isEmpty(), "The error output must be empty")
                Assertions.assertTrue(stdOut.startsWith(
                        "{\"${textUrl.canonicalPath}\":{\"name\":\"root\",\"from\":\"0\",\"to\":\"0\"}}"),
                        "The output is incorrect")
            }
        }

        exitProcess(0)
    }
}
