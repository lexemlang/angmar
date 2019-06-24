package org.lexem.angmar.utils

import org.junit.jupiter.api.*
import org.lexem.angmar.nodes.ParserNode

/**
 * Checks that a node has type and executes more checks inside it.
 */
internal inline fun <reified T : ParserNode> assertNodeType(node: ParserNode, checker: (T) -> Unit) {
    Assertions.assertTrue(node is T)
    checker(node as T)
}