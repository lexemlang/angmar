package org.lexem.angmar.compiler.literals

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*
import java.util.*

/**
 * Compiler for [BitlistElementNode].
 */
internal object BitlistElementCompiler {
    fun compile(parent: CompiledNode, parentSignal: Int, node: BitlistElementNode): CompiledNode {
        if (node.expression != null) {
            return node.expression!!.compile(parent, parentSignal).changeParserNode(node)
        }

        // Add value to the bitlist.
        val number = node.number!!

        val result = when (node.radix) {
            16 -> {
                val bitSet = BitSet(number.length * 4)
                val rest = LxmBitList(BitList(number.length * 4, bitSet))

                var index = 0
                for (c in number) {
                    val code = when {
                        c >= 'a' -> c - 'a' + 10
                        c >= 'A' -> c - 'A' + 10
                        else -> c - '0'
                    }

                    if (code.and(8) != 0) {
                        bitSet[index] = true
                    }

                    if (code.and(4) != 0) {
                        bitSet[index + 1] = true
                    }

                    if (code.and(2) != 0) {
                        bitSet[index + 2] = true
                    }

                    if (code.and(1) != 0) {
                        bitSet[index + 3] = true
                    }

                    index += 4
                }

                rest
            }
            8 -> {
                val bitSet = BitSet(number.length * 3)
                val rest = LxmBitList(BitList(number.length * 3, bitSet))

                var index = 0
                for (c in number) {
                    val code = c - '0'

                    if (code.and(4) != 0) {
                        bitSet[index] = true
                    }

                    if (code.and(2) != 0) {
                        bitSet[index + 1] = true
                    }

                    if (code.and(1) != 0) {
                        bitSet[index + 2] = true
                    }

                    index += 3
                }

                rest
            }
            2 -> {
                val bitSet = BitSet(number.length)
                val rest = LxmBitList(BitList(number.length, bitSet))

                var index = 0
                for (c in number) {
                    if (c == '1') {
                        bitSet[index] = true
                    }

                    index += 1
                }

                rest
            }
            else -> throw AngmarUnreachableException()
        }

        return ConstantCompiled(parent, parentSignal, node, result)
    }
}
