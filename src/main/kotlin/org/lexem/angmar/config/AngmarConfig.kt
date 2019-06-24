package org.lexem.angmar.config

/**
 * The Lexem's command configuration.
 */
data class AngmarConfig(
    // Config
    val debug: Boolean = false,
    val parallelCompiler: Boolean = false,
    val parallelInterpreter: Boolean = false,
    val parallelInterpreterBranches: Boolean = false,
    val forwardBuffer: Boolean = false,

    // Angmar
    val entryPoint: String = Consts.defaultEntryPoint,
    val outputFormat: String = Consts.AngmarOutputs.json
)