package org.lexem.angmar.io

import java.io.*

/**
 * Utilities to write and read files and folders.
 */
object IOUtils {
    /**
     * Writes a file creating its parent directories if they do not exist yet.
     */
    fun writeTextFile(file: File, content: String) {
        // Make parent folders.
        file.parentFile.mkdirs()

        file.writeText(content)
    }
}