package org.lexem.angmar.config

/**
 * Constants of the project.
 */
internal object Consts {
    const val projectVersion = "0.1.0"
    const val defaultEntryPoint = "main"

    object Commands {
        const val parser = "angmar"
    }

    object Logger {
        const val codeTitle = "Code"
        const val hintTitle = "Hint"
        const val errorIdTitle = "ERID"
    }

    object AngmarOutputs {
        const val json = "json"
        const val tree = "tree"
    }

    object Files {
        const val lexemFileExtension = ".lxm"
        const val outputFileJsonExtension = ".${AngmarOutputs.json}"
        const val outputFileTreeExtension = ".${AngmarOutputs.tree}"
    }
}
