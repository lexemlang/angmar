package org.lexem.angmar.commands

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import com.google.gson.*
import es.jtp.kterm.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.io.*
import kotlin.system.*

/**
 * The main command of Angmar.
 */
internal class AngmarCommand : CliktCommand(name = AngmarCommand, help = "Manages a Lexem project.") {

    private val debug by option(debugLongOption, help = "Allows the logger to print the debug level.").flag(
            default = false)

    private val entryPoint by option(entryPointLongOption, help = "The entry point of the grammar.").default(
            Consts.defaultEntryPoint)

    private val timeout by option(timeoutLongOption,
            help = "The timeout for the analyzer.").convert { it.toLong() }.default(
            Consts.Analyzer.defaultTimeoutInMilliseconds)

    private val output by option(outputShortOption, outputLongOption,
            help = "Where to save the output of the analyzer.").file()

    private val grammarSource by argument(name = grammarSourceArgument, help = "The main file of the grammar.").file(
            exists = true, folderOkay = false, readable = true)

    private val texts by argument(name = textsArgument, help = "The files to parse with the specified grammar.").file(
            exists = true, folderOkay = false, readable = true).multiple(true).unique()

    init {
        versionOption(Consts.projectVersion, names = setOf("-v", "--version"))
    }

    override fun run() {
        // Exits if there are no texts to analyze.
        if (texts.isEmpty()) {
            Logger.warn("There are no texts to analyze. Exiting the application.")
            return
        }

        // Parse the grammar.
        val grammarReader = IOStringReader.from(grammarSource)
        val parser = LexemParser(grammarReader)

        conditionalDebugLog(debug) {
            "Parsing main grammar file: ${grammarSource.canonicalPath}"
        }

        var grammarRootNode: LexemFileNode? = null

        val timeParsing = TimeUtils.measureTimeSeconds {
            try {
                grammarRootNode = LexemFileNode.parse(parser)
            } catch (e: AngmarParserException) {
                e.logMessage()
            }
        }

        if (grammarRootNode == null) {
            Logger.error("Cannot parse the main grammar file (${grammarSource.canonicalPath}) as Lexem") {
                showDate = true
            }

            return
        }

        conditionalDebugLog(debug) {
            "Finished in $timeParsing seconds"
        }

        // Compile the grammar.
        conditionalDebugLog(debug) {
            "Compiling main grammar file: ${grammarSource.canonicalPath}"
        }

        var compiledGrammarRootNode: LexemFileCompiled? = null

        val timeCompiling = TimeUtils.measureTimeSeconds {
            try {
                compiledGrammarRootNode = LexemFileCompiled.compile(grammarRootNode!!)
            } catch (e: AngmarCompilerException) {
                e.logMessage()
            }
        }

        if (grammarRootNode == null) {
            Logger.error("Cannot compile the main grammar file (${grammarSource.canonicalPath})") {
                showDate = true
            }

            return
        }

        conditionalDebugLog(debug) {
            "Finished in $timeCompiling seconds"
        }

        // Analyze each of the texts with the parsed grammar.
        val analyzer = LexemAnalyzer(compiledGrammarRootNode!!)
        val results = mutableListOf<Pair<File, JsonObject?>>()

        conditionalDebugLog(debug) {
            "Executing the analysis:"
        }

        val timeAnalyzing = TimeUtils.measureTimeSeconds {
            val count = texts.size.toString()
            for (i in texts.withIndex()) {
                conditionalDebugLog(debug) {
                    "  [${(i.index + 1).toString().padStart(
                            count.length)}/$count] Analyzing file: ${i.value.canonicalPath}"
                }

                val timeFile = TimeUtils.measureTimeSeconds {
                    val textReader = IOStringReader.from(i.value)

                    analyzer.setEntryPoint(entryPoint)

                    val finalTimeout = if (debug) {
                        Consts.Analyzer.defaultTestTimeoutInMilliseconds
                    } else {
                        timeout
                    }
                    var currentTime = 0L

                    try {
                        var result: JsonObject? = null
                        while (currentTime < finalTimeout) {
                            var hasFinished = false
                            val subTime = measureTimeMillis {
                                hasFinished = if (currentTime == 0L) {
                                    analyzer.start(textReader,
                                            timeoutInMilliseconds = Consts.Commands.debugLogEachMilliseconds)
                                } else {
                                    analyzer.resume(timeoutInMilliseconds = Consts.Commands.debugLogEachMilliseconds)
                                }
                            }

                            currentTime += subTime

                            if (hasFinished) {
                                result = analyzer.getResult().toTree()
                                break
                            } else {
                                conditionalDebugLog(debug) {
                                    "  [${(i.index + 1).toString().padStart(
                                            count.length)}/$count] Analyzing file: ${i.value.canonicalPath} - ${currentTime / 1000.0} seconds - ticks[${analyzer.ticks}]"
                                }
                            }
                        }

                        results.add(Pair(i.value, result))
                    } catch (e: AngmarAnalyzerException) {
                        if (e.type == AngmarAnalyzerExceptionType.CustomError) {
                            e.logger.logAsWarn()

                            val result = JsonObject()

                            result.addProperty("isError", true)
                            result.addProperty("type", "analyzer")
                            result.addProperty("id", e.type.name)
                            result.addProperty("message", e.logger.toString(LogLevel.Error))

                            results.add(Pair(i.value, result))
                        } else {
                            throw e
                        }
                    }
                }

                conditionalDebugLog(debug) {
                    "  [${(i.index + 1).toString().padStart(count.length)}/$count] Finished in $timeFile seconds"
                }
            }
        }

        conditionalDebugLog(debug) {
            "Finished in $timeAnalyzing seconds"
        }

        // Print the output.
        conditionalDebugLog(debug) {
            "Printing the output:"
        }

        val timeWritingOutput = TimeUtils.measureTimeSeconds {
            val count = results.size.toString()
            val printer = JsonObject()

            for (i in results.withIndex()) {
                val fileAndResults = i.value
                conditionalDebugLog(debug) {
                    "  [${(i.index + 1).toString().padStart(
                            count.length)}/$count] Printings file: ${fileAndResults.first.canonicalPath}"
                }

                val timeFile = TimeUtils.measureTimeSeconds {
                    printer.add(fileAndResults.first.canonicalPath, fileAndResults.second)
                }

                conditionalDebugLog(debug) {
                    "  [${(i.index + 1).toString().padStart(count.length)}/$count] Finished in $timeFile seconds"
                }
            }

            if (output != null) {
                output!!.writeText(printer.toString())
            } else {
                println(printer.toString())
            }
        }

        conditionalDebugLog(debug) {
            "Finished in $timeWritingOutput seconds"
        }

        val statsFull = """
                Parsing main grammar:   ${timeParsing}s
                Compiling main grammar: ${timeCompiling}s
                Analyzing grammar:      ${timeAnalyzing}s
                  - Average by file:    ${timeAnalyzing / texts.size}s
                Writing output:         ${timeWritingOutput}s
                  - Average by file:    ${timeWritingOutput / texts.size}s
            """.trimIndent()
        val statsEnd = """
                Total:                  ${timeParsing + timeCompiling + timeAnalyzing + timeWritingOutput}s
            """.trimIndent()
        Logger.info("Summary") {
            showDate = true
            addSourceCode(statsFull) {
                title = "Stats"
                highlightSection(0, statsFull.length - 1)
            }
            addSourceCode(statsEnd) {
                highlightSection(0, statsEnd.length - 1)
            }
        }
    }

    // STATIC -----------------------------------------------------------------

    companion object {
        const val AngmarCommand = "angmar"

        const val debugLongOption = "--debug"
        const val entryPointLongOption = "--entry-point"
        const val timeoutLongOption = "--timeout"
        const val outputShortOption = "-o"
        const val outputLongOption = "--output"

        const val grammarSourceArgument = "GRAMMAR_SOURCE"
        const val textsArgument = "TEXTS_TO_ANALYZE"

        /**
         * Prints a DEBUG log depending on the condition.
         */
        private fun conditionalDebugLog(condition: Boolean, messageBuilder: () -> String) {
            if (condition) {
                Logger.debug(messageBuilder()) {
                    showDate = true
                }
            }
        }
    }
}
