package org.lexem.angmar.commands

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import com.google.gson.*
import es.jtp.kterm.*
import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.io.readers.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.utils.*
import java.io.*

/**
 * The main command of Angmar.
 */
internal class AngmarCommand : CliktCommand(name = AngmarCommand, help = "Manages a Lexem project.") {

    private val debug by option(debugLongOption, help = "Allows the logger to print the debug level.").flag(
            default = false)

    private val forwardBuffer by option(forwardBufferShortOption, forwardBufferLongOption,
            help = "Disables the use of the forward buffer during parsing.").flag(default = true)

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
        val parser = LexemParser(grammarReader, forwardBuffer)

        conditionalDebugLog(debug) {
            "Parsing main grammar file: ${grammarSource.canonicalPath}"
        }

        var grammarRootNode: LexemFileNode? = null

        var time = TimeUtils.measureTimeSeconds {
            grammarRootNode = LexemFileNode.parse(parser)
        }

        if (grammarRootNode == null) {
            Logger.error("Cannot parse the main grammar file (${grammarSource.canonicalPath}) as Lexem") {
                showDate = true
            }

            return
        }

        conditionalDebugLog(debug) {
            "Finished in $time seconds"
        }

        // Analyze each of the texts with the parsed grammar.
        val analyzer = LexemAnalyzer(grammarRootNode!!)
        val results = mutableListOf<Pair<File, LexemMatch?>>()

        conditionalDebugLog(debug) {
            "Executing the analysis:"
        }

        time = TimeUtils.measureTimeSeconds {
            val count = texts.size.toString()
            for (i in texts.withIndex()) {
                conditionalDebugLog(debug) {
                    "  [${(i.index + 1).toString().padStart(
                            count.length)}/$count] Analyzing file: ${i.value.canonicalPath}"
                }

                time = TimeUtils.measureTimeSeconds {
                    val textReader = IOStringReader.from(i.value)

                    analyzer.setEntryPoint(entryPoint)

                    val result = if (analyzer.start(textReader, timeout)) {
                        null
                    } else {
                        analyzer.getResult()
                    }

                    results.add(Pair(i.value, result))
                }

                conditionalDebugLog(debug) {
                    "  [${(i.index + 1).toString().padStart(count.length)}/$count] Finished in $time seconds"
                }
            }
        }

        conditionalDebugLog(debug) {
            "Finished in $time seconds"
        }

        // Print the output.
        conditionalDebugLog(debug) {
            "Printing the output:"
        }

        time = TimeUtils.measureTimeSeconds {
            val count = results.size.toString()
            val printer = JsonObject()

            for (i in results.withIndex()) {
                val fileAndResults = i.value
                conditionalDebugLog(debug) {
                    "  [${(i.index + 1).toString().padStart(
                            count.length)}/$count] Printings file: ${fileAndResults.first.canonicalPath}"
                }

                time = TimeUtils.measureTimeSeconds {
                    printer.add(fileAndResults.first.canonicalPath, fileAndResults.second?.toTree())
                }

                conditionalDebugLog(debug) {
                    "  [${(i.index + 1).toString().padStart(count.length)}/$count] Finished in $time seconds"
                }
            }

            if (output != null) {
                output!!.writeText(printer.toString())
            } else {
                println(printer.toString())
            }
        }

        conditionalDebugLog(debug) {
            "Finished in $time seconds"
        }
    }

    // STATIC -----------------------------------------------------------------

    companion object {
        const val AngmarCommand = "angmar"

        const val debugLongOption = "--debug"
        const val forwardBufferShortOption = "-dfb"
        const val forwardBufferLongOption = "--disable-forward-buffer"
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
