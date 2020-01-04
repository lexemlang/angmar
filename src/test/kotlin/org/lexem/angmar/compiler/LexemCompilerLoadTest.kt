package org.lexem.angmar.compiler

import org.junit.jupiter.api.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import org.lexem.angmar.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.util.stream.*
import kotlin.streams.*

internal class LexemCompilerLoadTest {
    // PARAMETERS -------------------------------------------------------------

    companion object {
        @JvmStatic
        private fun provideFileStatements(): Stream<Arguments> {
            val result = """
                {var \(35) = 35}
                if a == 35 {
                    while a == 35 {
                        b += 35
                        return 35
                    }
                }
                when { 
                    if 35 { for (#testId) in 35 {35} }
                }
            """.trimIndent()

            val str = result + "\n"

            return listOf(Arguments.of(str)).asSequence().asStream()
        }
    }


    // TESTS ------------------------------------------------------------------

    @ParameterizedTest
    @Nightly
    @MethodSource("provideFileStatements")
    fun `parse 100 lines`(text: String) {
        val finalText = text.repeat(10)
        val parser = LexemParser(IOStringReader.from(finalText))
        val res = LexemFileNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemFileNode

        val time = TimeUtils.measureTimeSeconds {
            LexemFileCompiled.compile(res)
        }

        println("The time for 100 lines is ${time}s")
        Assertions.assertTrue(time <= 0.05, "The time is excessive")
    }

    @ParameterizedTest
    @Nightly
    @MethodSource("provideFileStatements")
    fun `parse 1000 lines`(text: String) {
        val finalText = text.repeat(100)
        val parser = LexemParser(IOStringReader.from(finalText))
        val res = LexemFileNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemFileNode

        val time = TimeUtils.measureTimeSeconds {
            LexemFileCompiled.compile(res)
        }

        println("The time for 1000 lines is ${time}s")
        Assertions.assertTrue(time <= 0.05, "The time is excessive")
    }

    @ParameterizedTest
    @Nightly
    @MethodSource("provideFileStatements")
    fun `parse 10000 lines`(text: String) {
        val finalText = text.repeat(1000)
        val parser = LexemParser(IOStringReader.from(finalText))
        val res = LexemFileNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemFileNode

        val time = TimeUtils.measureTimeSeconds {
            LexemFileCompiled.compile(res)
        }

        println("The time for 10000 lines is ${time}s")
        Assertions.assertTrue(time <= 0.05, "The time is excessive")
    }

    @ParameterizedTest
    @Nightly
    @MethodSource("provideFileStatements")
    fun `parse 100000 lines`(text: String) {
        val finalText = text.repeat(10000)
        val parser = LexemParser(IOStringReader.from(finalText))
        val res = LexemFileNode.parse(parser)

        Assertions.assertNotNull(res, "The input has not been correctly parsed")
        res as LexemFileNode

        val time = TimeUtils.measureTimeSeconds {
            LexemFileCompiled.compile(res)
        }

        println("The time for 100000 lines is ${time}s")
        Assertions.assertTrue(time <= 0.2, "The time is excessive")
    }
}
