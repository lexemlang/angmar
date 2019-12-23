package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.errors.*

/**
 * The Lexem value of a position in code.
 */
internal open class LxmCodePoint(val node: CompiledNode, val signal: Int, val callerNode: CompiledNode,
        val callerContextName: String) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() =
            "[Code Point] (signal: $signal, node: $node, callerNode: $callerNode, callerContextName: $callerContextName)"
}
