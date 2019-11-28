package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.parser.literals.*
import java.util.*

/**
 * The Lexem values of the BitList type.
 */
internal class LxmBitList(val size: Int, val primitive: BitSet) : LexemPrimitive {

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory)
        return context.getPropertyValue(memory, BitListType.TypeName) as LxmReference
    }

    override fun getHashCode(memory: LexemMemory) = primitive.hashCode()

    override fun toString(): String {
        val result = StringBuilder()
        when {
            size % 4 == 0 -> {
                result.append(BitlistNode.hexadecimalPrefix)
                result.append(BitlistNode.startToken)

                for (i in 0 until size step 4) {
                    var digit = 0

                    for (j in 0 until 4) {
                        digit = digit.shl(1)

                        if (primitive[i + j]) {
                            digit += 1
                        }
                    }

                    result.append(digit.toString(16))
                }
            }
            size % 3 == 0 -> {
                result.append(BitlistNode.octalPrefix)
                result.append(BitlistNode.startToken)

                for (i in 0 until size step 3) {
                    var digit = 0

                    for (j in 0 until 3) {
                        digit = digit.shl(1)

                        if (primitive[i + j]) {
                            digit += 1
                        }
                    }

                    result.append(digit.toString(8))
                }
            }
            else -> {
                result.append(BitlistNode.binaryPrefix)
                result.append(BitlistNode.startToken)

                for (i in 0 until size) {
                    result.append(if (primitive[i]) {
                        '1'
                    } else {
                        '0'
                    })
                }
            }
        }

        result.append(BitlistNode.endToken)
        return result.toString()
    }

    // STATIC -----------------------------------------------------------------

    companion object {
        val Empty = LxmBitList(0, BitSet())
    }
}
