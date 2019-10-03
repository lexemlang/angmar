package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The lexem value of the Nil type.
 */
internal object LxmNil : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory) =
            AnalyzerCommons.getStdLibContextElement<LxmObject>(memory, NilType.TypeName)

    override fun getHashCode(memory: LexemMemory) = null.hashCode()

    override fun toString() = "nil"
}
