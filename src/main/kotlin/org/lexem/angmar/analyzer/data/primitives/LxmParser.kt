package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * The Lexem value of a parser.
 */
internal open class LxmParser(val parser: LexemParser) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() = "[Parser] (parser: $parser)"
}
