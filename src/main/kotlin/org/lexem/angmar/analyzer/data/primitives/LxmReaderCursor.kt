package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*

/**
 * The Lexem value of a position of the text.
 */
internal class LxmReaderCursor(val primitive: IReaderCursor) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() = "[Reader cursor](primitive: $primitive)"
}
