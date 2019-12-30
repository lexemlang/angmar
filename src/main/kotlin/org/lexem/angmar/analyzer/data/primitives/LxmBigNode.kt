package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*

/**
 * The Lexem values of a [BigNode] value.
 */
internal class LxmBigNode(val node: BigNode) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode() = node.hashCode()

    override fun toString() = node.toString()
}
