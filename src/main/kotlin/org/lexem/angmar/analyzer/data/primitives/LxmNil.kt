package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The Lexem value of the Nil type.
 */
internal object LxmNil : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, NilType.TypeName) as LxmReference
    }

    override fun getHashCode() = null.hashCode()

    override fun toLexemString(memory: IMemory) = LxmString.Nil

    override fun toString() = "nil"
}
