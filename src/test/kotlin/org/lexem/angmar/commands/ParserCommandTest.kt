package org.lexem.angmar.commands

import com.ginsberg.junit.exit.*
import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.utils.*
import java.io.*

@Disabled
internal class AngmarCommandTest {
    //    private val oriOut = System.out
    //    private val oriErrOut = System.err

    @Test
    @ExpectSystemExit
    fun `version`() {
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val args = arrayOf("--version")
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertTrue(standardOutput.contains(Consts.Commands.parser))
            Assertions.assertTrue(standardOutput.contains(Consts.projectVersion))
            Assertions.assertTrue(errorOutput.isEmpty())
        }

        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val args = arrayOf("-v")
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertTrue(standardOutput.contains(Consts.Commands.parser))
            Assertions.assertTrue(standardOutput.contains(Consts.projectVersion))
            Assertions.assertTrue(errorOutput.isEmpty())
        }
    }

    @Test
    fun `one file - output in the same folder`() {
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val url = Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos")
            val file = File(url.path).canonicalFile

            val args = arrayOf(file.path)
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertFalse(standardOutput.contains("debug", true))
            Assertions.assertFalse(standardOutput.contains("error", true))
            Assertions.assertTrue(standardOutput.contains(file.path))
            Assertions.assertTrue(errorOutput.isEmpty())

            // Check files.
            val outputFile = file.parentFile.resolve("test1.mos${Consts.Files.outputFileJsonExtension}")
            Assertions.assertTrue(outputFile.exists())
            Assertions.assertTrue(outputFile.isFile)

            outputFile.delete()
        }

        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val url = Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos")
            val file = File(url.path).canonicalFile

            val args = arrayOf("-fmt=${Consts.AngmarOutputs.json}", file.path)
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertFalse(standardOutput.contains("debug", true))
            Assertions.assertFalse(standardOutput.contains("error", true))
            Assertions.assertTrue(standardOutput.contains(file.path))
            Assertions.assertTrue(errorOutput.isEmpty())

            // Check files.
            val outputFile = file.parentFile.resolve("test1.mos${Consts.Files.outputFileJsonExtension}")
            Assertions.assertTrue(outputFile.exists())
            Assertions.assertTrue(outputFile.isFile)

            outputFile.delete()
        }

        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val url = Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos")
            val file = File(url.path).canonicalFile

            val args = arrayOf("-fmt=${Consts.AngmarOutputs.tree}", file.path)
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertFalse(standardOutput.contains("debug", true))
            Assertions.assertFalse(standardOutput.contains("error", true))
            Assertions.assertTrue(standardOutput.contains(file.path))
            Assertions.assertTrue(errorOutput.isEmpty())

            // Check files.
            val outputFile = file.parentFile.resolve("test1.mos${Consts.Files.outputFileTreeExtension}")
            Assertions.assertTrue(outputFile.exists())
            Assertions.assertTrue(outputFile.isFile)

            outputFile.delete()
        }
    }

    @Test
    fun `more file - output in the same folder`() {
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val urls = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/test2.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test3.mos"))

            val files = urls.map { File(it.path).canonicalFile }

            val args = files.map { it.path }
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertFalse(standardOutput.contains("debug", true))
            Assertions.assertFalse(standardOutput.contains("error", true))
            files.forEach { file ->
                Assertions.assertTrue(standardOutput.contains(file.path))
            }
            Assertions.assertTrue(errorOutput.isEmpty())

            // Check files.
            files.map { it.parentFile.resolve("${it.name}${Consts.Files.outputFileJsonExtension}") }
                    .forEach { outputFile ->
                        Assertions.assertTrue(outputFile.exists())
                        Assertions.assertTrue(outputFile.isFile)
                        outputFile.delete()
                    }
        }
    }

    @Test
    fun `more files and folders - output in the same folder`() {
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val urlFiles = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/test2.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test3.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test4.mos"))

            val urlFolders = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib"))

            val files = urlFiles.map { File(it.path).canonicalFile }
            val folders = urlFolders.map { File(it.path).canonicalFile }

            val args = (files + folders).map { it.path }
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertFalse(standardOutput.contains("debug", true))
            Assertions.assertFalse(standardOutput.contains("error", true))
            files.forEach { file ->
                Assertions.assertTrue(standardOutput.contains(file.path))
            }
            Assertions.assertTrue(errorOutput.isEmpty())

            // Check files.
            files.map { it.parentFile.resolve("${it.name}${Consts.Files.outputFileJsonExtension}") }
                    .forEach { outputFile ->
                        Assertions.assertTrue(outputFile.exists())
                        Assertions.assertTrue(outputFile.isFile)
                        outputFile.delete()
                    }
        }

        // Only folder
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val urlFiles = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/test2.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test3.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test4.mos"))

            val urlFolders = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src"))

            val files = urlFiles.map { File(it.path).canonicalFile }
            val folders = urlFolders.map { File(it.path).canonicalFile }

            val args = folders.map { it.path }
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertFalse(standardOutput.contains("debug", true))
            Assertions.assertFalse(standardOutput.contains("error", true))
            files.forEach { file ->
                Assertions.assertTrue(standardOutput.contains(file.path))
            }
            Assertions.assertTrue(errorOutput.isEmpty())

            // Check files.
            files.map { it.parentFile.resolve("${it.name}${Consts.Files.outputFileJsonExtension}") }
                    .forEach { outputFile ->
                        Assertions.assertTrue(outputFile.exists())
                        Assertions.assertTrue(outputFile.isFile)
                        outputFile.delete()
                    }
        }
    }

    @Test
    fun `more files and folders - output in other folder`() {
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val urlFiles = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/test2.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test3.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test4.mos"))

            val urlFolders = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib"))
            val urlOutput = Thread.currentThread().contextClassLoader.getResource("./test-project/")

            val files = urlFiles.map { File(it.path).canonicalFile }
            val folders = urlFolders.map { File(it.path).canonicalFile }
            val outputFolder = File(urlOutput.path, "out").canonicalFile

            val args = (files + folders).map { it.path } + listOf("-o=${outputFolder.path}")
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertFalse(standardOutput.contains("debug", true))
            Assertions.assertFalse(standardOutput.contains("error", true))
            files.forEach { file ->
                Assertions.assertTrue(standardOutput.contains(file.path))
            }
            Assertions.assertTrue(errorOutput.isEmpty())

            // Check files.
            files.map { outputFolder.resolve("${it.name}${Consts.Files.outputFileJsonExtension}") }
                    .forEach { outputFile ->
                        Assertions.assertTrue(outputFile.exists())
                        Assertions.assertTrue(outputFile.isFile)
                    }

            outputFolder.deleteRecursively()
        }

        // The same but keeping the file structure.
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val urlFiles = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/test2.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test3.mos"),
                    Thread.currentThread().contextClassLoader.getResource("./test-project/src/lib/test4.mos"))

            val urlFolders = listOf(Thread.currentThread().contextClassLoader.getResource("./test-project/src/"))
            val urlOutput = Thread.currentThread().contextClassLoader.getResource("./test-project/")

            val files = urlFiles.map { File(it.path).canonicalFile }
            val folders = urlFolders.map { File(it.path).canonicalFile }
            val outputFolder = File(urlOutput.path, "out").canonicalFile

            val args = folders.map { it.path } + listOf("-o=${outputFolder.path}")
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertFalse(standardOutput.contains("debug", true))
            Assertions.assertFalse(standardOutput.contains("error", true))
            files.forEach { file ->
                Assertions.assertTrue(standardOutput.contains(file.path))
            }
            Assertions.assertTrue(errorOutput.isEmpty())

            // Check files.

            files.map {
                outputFolder.resolve(it.relativeTo(folders.first()).parentFileOrRelativeRoot())
                        .resolve("${it.name}${Consts.Files.outputFileJsonExtension}")
            }.forEach { outputFile ->
                Assertions.assertTrue(outputFile.exists())
                Assertions.assertTrue(outputFile.isFile)
            }

            outputFolder.deleteRecursively()
        }
    }

    @Test
    @ExpectSystemExit
    fun `no source`() {
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val args = arrayOf(Consts.Commands.parser)
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertTrue(standardOutput.isEmpty())
            Assertions.assertTrue(errorOutput.startsWith("Usage:"))
            Assertions.assertTrue(errorOutput.contains("Error: Invalid value for \"SOURCES\""))
        }
    }

    @Test
    @Incorrect
    @ExpectSystemExit
    fun `incorrect output format`() {
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val url = Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos")

            val args = arrayOf("-fmt=test", url.path)
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertTrue(standardOutput.isEmpty())
            Assertions.assertTrue(errorOutput.startsWith("Usage:"))
            Assertions.assertTrue(errorOutput.contains("Error: Invalid value for \"-fmt\""))
        }
    }

    @Test
    @Incorrect
    @ExpectSystemExit
    fun `incorrect lowercase output format`() {
        let {
            val myOut = ByteArrayOutputStream()
            val myErrorOut = ByteArrayOutputStream()
            System.setOut(PrintStream(myOut))
            System.setErr(PrintStream(myErrorOut))

            val url = Thread.currentThread().contextClassLoader.getResource("./test-project/src/test1.mos")

            val args = arrayOf("-fmt=${Consts.AngmarOutputs.json}", url.path)
            try {
                AngmarCommand().main(args)
            } catch (e: SecurityException) {
            }

            val standardOutput = myOut.toString().trim()
            val errorOutput = myErrorOut.toString().trim()

            Assertions.assertTrue(standardOutput.isEmpty())
            Assertions.assertTrue(errorOutput.startsWith("Usage:"))
            Assertions.assertTrue(errorOutput.contains("Error: Invalid value for \"-fmt\""))
        }
    }
}
