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
import org.lexem.angmar.io.*
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
    internal inline fun assertParserException(type: AngmarParserExceptionType?, fn: () -> Unit) {
        try {
            fn()
            throw Exception("This method should throw an AngmarParserException")
        } catch (e: AngmarParserException) {
            if (type != null && e.type != type) {
                throw AngmarException("The expected AngmarParserException is $type but actually it is ${e.type}", e)
            }

            e.logMessage()
        }
    }

    /**
     * Ensures an [AngmarAnalyzerException] or throws an error.
     */
    internal inline fun assertAnalyzerException(type: AngmarAnalyzerExceptionType?, print: Boolean = true,
            fn: () -> Unit) {
        try {
            fn()
            throw Exception("This method should throw an AngmarAnalyzerException")
        } catch (e: AngmarAnalyzerException) {
            if (type != null && e.type != type) {
                throw AngmarException("The expected AngmarAnalyzerException was $type but it is ${e.type}", e)
            }

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
            val controlValue = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
            Assertions.assertEquals(control, controlValue.type, "The type property is incorrect")
            Assertions.assertEquals(tagName, controlValue.tag, "The tag property is incorrect")
            Assertions.assertEquals(value, controlValue.value, "The value property is incorrect")

            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)
        }
    }

    /**
     * Creates a correct [LexemMemory].
     */
    internal fun generateTestMemory() = LexemMemory().apply { freezeCopy() }

    /**
     * Creates an analyzer from the specified parameter.
     */
    internal fun createAnalyzerFrom(grammarText: String, isDescriptiveCode: Boolean = false,
            isFilterCode: Boolean = false,
            parserFunction: (LexemParser, ParserNode, Int) -> ParserNode?): LexemAnalyzer {
        val parser = LexemParser(IOStringReader.from(grammarText))
        parser.isDescriptiveCode = isDescriptiveCode || isFilterCode
        parser.isFilterCode = isFilterCode
        val grammar = parserFunction(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(grammar, "The grammar cannot be null")

        return LexemAnalyzer(grammar!!)
    }

    /**
     * Creates an analyzer from the specified file.
     */
    internal fun createAnalyzerFromFile(filePath: String,
            parserFunction: (LexemParser, ParserNode, Int) -> ParserNode?): LexemAnalyzer {
        val parser = LexemParser(IOStringReader.from(File(filePath)))
        val grammar = parserFunction(parser, ParserNode.Companion.EmptyParserNode, 0)

        Assertions.assertNotNull(grammar, "The grammar cannot be null")

        return LexemAnalyzer(grammar!!)
    }

    /**
     * Executes the analyzer and checks its results are empty.
     */
    internal fun processAndCheckEmpty(analyzer: LexemAnalyzer, text: IReader = IOStringReader.from(""),
            status: LexemAnalyzer.ProcessStatus = LexemAnalyzer.ProcessStatus.Forward,
            hasBacktrackingData: Boolean = false, bigNodeCount: Int = 1) {
        val result = analyzer.start(text, timeoutInMilliseconds = 5 * 60 * 1000 /* 5 minutes */)

        // Assert status of the analyzer.
        Assertions.assertEquals(status, analyzer.processStatus, "The status is incorrect")
        Assertions.assertNull(analyzer.nextNode, "The next node must be null")
        Assertions.assertEquals(0, analyzer.signal, "The signal is incorrect")

        if (!hasBacktrackingData) {
            Assertions.assertNull(analyzer.backtrackingData, "The backtrackingData must be null")
        } else {
            Assertions.assertNotNull(analyzer.backtrackingData, "The backtrackingData cannot be null")
        }

        // Assert status of the result.
        Assertions.assertTrue(result, "The result must be true")

        // Check the memory has only n big nodes apart from the stdlib.
        var node = analyzer.memory.lastNode
        for (i in 0 until bigNodeCount) {
            Assertions.assertNotNull(node.previousNode, "The memory has lower than $bigNodeCount big nodes")
            node = node.previousNode!!
        }

        Assertions.assertNull(node.previousNode, "The memory has more than $bigNodeCount big nodes")
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

        // Remove from the stack.
        try {
            memory.removeFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint)
        } catch (e: AngmarAnalyzerException) {
        }

        // Check stack.
        if (memory.lastNode.actualStackSize != 0) {
            throw Exception(
                    "The stack must be empty. It contains ${memory.lastNode.actualStackSize} remaining elements in ${memory.lastNode.actualStackLevelSize} levels: ${memory.lastNode}")
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
        context.removePropertyIgnoringConstants(memory, AnalyzerCommons.Identifiers.HiddenLastResultNode)
        context.removePropertyIgnoringConstants(memory, AnalyzerCommons.Identifiers.HiddenRollbackCodePoint)

        // Check whether the context is empty.
        Assertions.assertEquals(0, context.getAllIterableProperties().size, "The context is not empty: $context")

        // Remove the stdlib context.
        val stdLibCell = memory.lastNode.getCell(LxmReference.StdLibContext.position)
        stdLibCell.decreaseReferences(memory, 1)

        // Check whether the memory is empty.
        Assertions.assertEquals(0, memory.lastNode.actualUsedCellCount,
                "The memory must be completely cleared. Remaining cells with values: ${memory.lastNode.actualUsedCellCount}")
    }
}
