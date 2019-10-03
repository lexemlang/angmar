package org.lexem.angmar.utils

import org.junit.jupiter.api.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.commands.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import java.io.*
import java.nio.file.*

object TestUtils {
    /**
     * Calls Angmar with the given arguments and returns the result of the console.
     */
    internal fun callAngmar(args: Array<String>, block: (stdOut: String, errOut: String) -> Unit) {
        val oriOut = System.out
        val oriErrOut = System.err
        val myOut = ByteArrayOutputStream()
        val myErrorOut = ByteArrayOutputStream()

        System.setOut(PrintStream(myOut))
        System.setErr(PrintStream(myErrorOut))

        try {
            AngmarCommand().main(args)
        } catch (e: SecurityException) {
        }

        System.setOut(oriOut)
        System.setErr(oriErrOut)

        val standardOutput = myOut.toString().trim()
        val errorOutput = myErrorOut.toString().trim()

        block(standardOutput, errorOutput)
    }

    /**
     * Ensures an [AngmarException] or throws an error.
     */
    internal inline fun assertAngmarException(print: Boolean = true, fn: () -> Unit) {
        try {
            fn()
            throw Exception("This method should throw an AngmarAnalyzerException")
        } catch (e: AngmarException) {
            if (print) {
                e.logMessage()
            }
        }
    }

    /**
     * Ensures an [AngmarParserException] or throws an error.
     */
    internal inline fun assertParserException(fn: () -> Unit) {
        try {
            fn()
            throw Exception("This method should throw an AngmarParserException")
        } catch (e: AngmarParserException) {
            e.logMessage()
        }
    }

    /**
     * Ensures an [AngmarAnalyzerException] or throws an error.
     */
    internal inline fun assertAnalyzerException(print: Boolean = true, fn: () -> Unit) {
        try {
            fn()
            throw Exception("This method should throw an AngmarAnalyzerException")
        } catch (e: AngmarAnalyzerException) {
            if (print) {
                e.logMessage()
            }
        }
    }

    /**
     * Ensures an [AngmarAnalyzerException] that raise the [AngmarAnalyzerExceptionType.TestControlSignalRaised] type.
     */
    internal inline fun assertControlSignalRaisedCheckingStack(analyzer: LexemAnalyzer, control: String,
            tagName: String?, value: LexemPrimitive?, fn: () -> Unit) {
        try {
            fn()
            throw Exception("This method should throw an AngmarAnalyzerException")
        } catch (e: AngmarAnalyzerException) {
            if (e.type != AngmarAnalyzerExceptionType.TestControlSignalRaised) {
                throw e
            }

            // Check stack.
            val controlValue = analyzer.memory.popStack() as LxmControl
            Assertions.assertEquals(control, controlValue.type, "The type property is incorrect")
            Assertions.assertEquals(tagName, controlValue.tag, "The tag property is incorrect")
            Assertions.assertEquals(value, controlValue.value, "The value property is incorrect")
        }
    }

    /**
     * Creates a correct [LexemMemory].
     */
    internal fun generateTestMemory() = LexemMemory().apply { freezeCopy() }

    /**
     * Creates an analyzer from the specified parameter.
     */
    internal fun createAnalyzerFrom(grammarText: String,
            parserFunction: (LexemParser, ParserNode, Int) -> ParserNode?): LexemAnalyzer {
        val parser = LexemParser(CustomStringReader.from(grammarText))
        val grammar = parserFunction(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(grammar, "The grammar cannot be null")

        return LexemAnalyzer(grammar!!)
    }

    /**
     * Creates an analyzer from the specified file.
     */
    internal fun createAnalyzerFromFile(filePath: String,
            parserFunction: (LexemParser, ParserNode, Int) -> ParserNode?): LexemAnalyzer {
        val parser = LexemParser(CustomStringReader.from(File(filePath)))
        val grammar = parserFunction(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(grammar, "The grammar cannot be null")

        return LexemAnalyzer(grammar!!)
    }

    /**
     * Executes the analyzer and checks its results are empty.
     */
    internal fun processAndCheckEmpty(analyzer: LexemAnalyzer) {
        val results = analyzer.process(CustomStringReader.from(""))

        // Assert status of the analyzer.
        Assertions.assertEquals(LexemAnalyzer.AnalyzerStatus.Forward, analyzer.status, "The status is incorrect")
        Assertions.assertNull(analyzer.nextNode, "The next node must be null")
        Assertions.assertEquals(0, analyzer.signal, "The signal is incorrect")

        // Assert status of the result.
        Assertions.assertTrue(results.isEmpty(), "The number of results is incorrect")
    }

    /**
     * Creates a temporary file.
     */
    internal fun createTempFile(fileName: String, text: String): File {
        val directory = System.getProperty("java.io.tmpdir")!!
        val file = Paths.get(directory, fileName).toAbsolutePath().toFile()

        file.writeText(text)

        return file
    }

    /**
     * Handles the creation and removal of a temporary file.
     */
    internal fun handleTempFiles(content: Map<String, String>, function: (Map<String, File>) -> Unit) {
        val files = content.mapValues { createTempFile(it.key, it.value) }
        try {
            function(files)
            files.forEach { it.value.delete() }
        } catch (e: Throwable) {
            files.forEach { it.value.delete() }

            throw e
        }
    }

    /**
     * Checks that the stack is empty and the context is the standard one.
     */
    internal fun checkEmptyStackAndContext(analyzer: LexemAnalyzer, valuesToRemove: List<String>? = null) {
        val memory = analyzer.memory

        // Check stack.
        try {
            memory.popStack()
            throw Exception("The stack must be empty")
        } catch (e: AngmarAnalyzerException) {
        }

        // Check context.
        val stdLibContext = AnalyzerCommons.getStdLibContext(memory)
        val context = AnalyzerCommons.getCurrentContext(memory)

        Assertions.assertEquals(stdLibContext, context, "The context must be the stdLib")

        // Remove elements from the context.
        StdlibCommons.GlobalNames.forEach {
            context.removePropertyIgnoringConstants(memory, it)
        }

        valuesToRemove?.forEach {
            context.removePropertyIgnoringConstants(memory, it)
        }

        context.removePropertyIgnoringConstants(memory, AnalyzerCommons.Identifiers.HiddenFileMap)
        context.removePropertyIgnoringConstants(memory, AnalyzerCommons.Identifiers.EntryPoint)
        context.removePropertyIgnoringConstants(memory, AnalyzerCommons.Identifiers.HiddenCurrentContext)

        // Check whether the context is empty.
        Assertions.assertEquals(0, context.getAllIterableProperties().size, "The context is not empty")

        // Remove the stdlib context.
        val stdLibCell = memory.lastNode.getCell(LxmReference.StdLibContext.position)
        stdLibCell.decreaseReferenceCount(memory, 1)

        // Check whether the memory is empty.
        Assertions.assertEquals(0, memory.lastNode.actualUsedCellCount, "The memory must be completely cleared")
    }
}
