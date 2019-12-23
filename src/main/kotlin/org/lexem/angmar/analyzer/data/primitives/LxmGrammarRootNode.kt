package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.errors.*

/**
 * The Lexem value of a grammar root node.
 */
internal open class LxmGrammarRootNode(val grammarRootNode: CompiledNode) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() = "[Parser] (root node: $grammarRootNode)"
}
