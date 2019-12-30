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

    override fun getType(bigNode: BigNode): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(bigNode, toWrite = false)
        return context.getPropertyValue(BitListType.TypeName) as LxmReference
    }

    override fun getHashCode() = primitive.hashCode()

    override fun toLexemString(bigNode: BigNode) = LxmString.from(toString())

    override fun toString() = primitive.toString()

    // STATIC -----------------------------------------------------------------

    companion object {
        val Empty = LxmBitList(BitList.Empty)
    }
}
