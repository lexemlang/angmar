package org.lexem.angmar.commands

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.groups.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import org.lexem.angmar.config.*

/**
 * The main command of Angmar.
 */
internal class AngmarCommand : CliktCommand(name = "angmar", help = "Manages a lexem project.") {

    private val debug by option("--debug", help = "Allows the logger to print the debug level.").flag(default = false)

    private val parallel by option("-p", "--parallel", help = "Enable or disable all parallel configuration.").flag(
            default = false)

    private val parallelCompiler by option("-pc", "--parallel-compiler",
            help = "Whether the compiler must run in parallel or not.").flag(default = false)

    private val interpreterOptions by InterpreterOptions().cooccurring()

    private val forwardBuffer by option("-fb", "--forward-buffer",
            help = "Whether to use the forward buffer during parsing or not.").flag(default = false)

    private val entryPoint by option("-ep", "--entry-point", help = "The entry point of the grammar.").default(
            Consts.defaultEntryPoint)

    private val outputFormat by option(help = "The format in which print the output file.").switch(
            "--o-${Consts.AngmarOutputs.json}" to Consts.AngmarOutputs.json,
            "--o-${Consts.AngmarOutputs.tree}" to Consts.AngmarOutputs.tree).default(Consts.AngmarOutputs.json)

    private val grammarSource by argument(help = "The main file of the grammar.").file(exists = true,
            folderOkay = false, readable = true)

    private val output by option("-o", "--output", help = "Where to save the output of the analyzer.").file()

    private val texts by argument(help = "The files to parse with the specified grammar.").file(exists = true,
            folderOkay = false, readable = true).multiple(true)

    init {
        versionOption(Consts.projectVersion, names = setOf("-v", "--version"))
        // subcommands()
    }

    override fun run() {
        // Create config.
        val config =
                AngmarConfig(debug, parallelCompiler || parallel, interpreterOptions?.parallel ?: false || parallel,
                        interpreterOptions?.parallelBranches ?: false || parallel, forwardBuffer,

                        entryPoint, outputFormat)

        // Execute actions.
    }

    // STATIC -----------------------------------------------------------------

    class InterpreterOptions : OptionGroup() {
        internal val parallel by option("-pi", "--parallel-interpreter",
                help = "Whether the interpreter must run in parallel or not.").flag(default = false)

        internal val parallelBranches by option("-pb", "--parallel-branches",
                help = "Whether the branches must be analyzed in parallel instead of using backtracking.").flag(
                default = false)
    }
}
