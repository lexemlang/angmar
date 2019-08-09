package org.lexem.angmar.utils

import com.github.ajalt.clikt.core.*
import java.io.*

/**
 * Gets the parent of this file if it has any or the root otherwise.
 */
fun File.parentFileOrRoot() = this.parentFile ?: File("/")


/**
 * Gets the parent of this file if it has any or the relative root otherwise.
 */
fun File.parentFileOrRelativeRoot() = this.parentFile ?: File("./")

/**
 * Gets the parent command as the specified type.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : CliktCommand> CliktCommand.parentAs() = this.context.parent!!.command as T