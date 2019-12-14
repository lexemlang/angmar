package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.data.*

/**
 * The Lexem values of the BitList type.
 */
internal class LxmBitList(val primitive: BitList) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, BitListType.TypeName) as LxmReference
    }

    override fun getHashCode(memory: LexemMemory) = primitive.hashCode()

    override fun toLexemString(memory: LexemMemory) = LxmString.from(toString())

    override fun toString() = primitive.toString()

    // STATIC -----------------------------------------------------------------

    companion object {
        val Empty = LxmBitList(BitList.Empty)
    }
}
