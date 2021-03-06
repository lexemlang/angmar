package org.lexem.angmar.config

/**
 * Constants of the project.
 */
internal object Consts {
    const val projectVersion = "0.1.1"
    const val defaultEntryPoint = "main"

    object Commands {
        const val parser = "angmar"
    }

    object Logger {
        const val codeTitle = "Code"
        const val hintTitle = "Hint"
        const val errorIdTitle = "ERID"
    }

    object Files {
        const val outputFileJsonExtension = ".json"
    }

    object Memory {
        const val maxPoolSize = 50
    }

    object Analyzer {
        const val defaultTimeoutInMilliseconds = 20000L // 20 seconds
    }
}
