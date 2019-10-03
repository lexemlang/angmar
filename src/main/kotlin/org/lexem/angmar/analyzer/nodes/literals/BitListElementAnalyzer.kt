package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*
import java.util.*


/**
 * Analyzer for Bitlist element literals.
 */
internal object BitListElementAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: BitlistElementNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Execute expression
                if (node.number == null) {
                    return analyzer.nextNode(node.expression!!)
                }

                // Add value to the bitlist.
                val prev = analyzer.memory.popStack() as LxmBitList
                val number = node.number!!
                val bitSet = BitSet()

                val result = when (node.radix) {
                    16 -> {
                        val result = LxmBitList(prev.size + number.length * 4, bitSet)
                        for (i in 0 until prev.size) {
                            result.primitive[i] = prev.primitive[i]
                        }

                        var index = 0
                        for (c in number) {
                            val code = when {
                                c >= 'a' -> c - 'a' + 10
                                c >= 'A' -> c - 'A' + 10
                                else -> c - '0'
                            }

                            if (code.and(8) != 0) {
                                bitSet[index + prev.size] = true
                            }

                            if (code.and(4) != 0) {
                                bitSet[index + prev.size + 1] = true
                            }

                            if (code.and(2) != 0) {
                                bitSet[index + prev.size + 2] = true
                            }

                            if (code.and(1) != 0) {
                                bitSet[index + prev.size + 3] = true
                            }

                            index += 4
                        }

                        result
                    }
                    8 -> {
                        val result = LxmBitList(prev.size + number.length * 3, bitSet)
                        for (i in 0 until prev.size) {
                            result.primitive[i] = prev.primitive[i]
                        }

                        var index = 0
                        for (c in number) {
                            val code = c - '0'

                            if (code.and(4) != 0) {
                                bitSet[index + prev.size] = true
                            }

                            if (code.and(2) != 0) {
                                bitSet[index + prev.size + 1] = true
                            }

                            if (code.and(1) != 0) {
                                bitSet[index + prev.size + 2] = true
                            }

                            index += 3
                        }

                        result
                    }
                    2 -> {
                        val result = LxmBitList(prev.size + number.length, bitSet)
                        for (i in 0 until prev.size) {
                            result.primitive[i] = prev.primitive[i]
                        }

                        var index = 0
                        for (c in number) {
                            if (c == '1') {
                                bitSet[index + prev.size] = true
                            }

                            index += 1
                        }

                        result
                    }
                    else -> throw AngmarUnreachableException()
                }

                analyzer.memory.pushStack(result)
            }
            signalEndExpression -> {
                // Check returned value is a bitlist.
                val right = analyzer.memory.popStack()

                if (right !is LxmBitList) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "${BitListType.TypeName} literals require that internal escaped expressions return a ${BitListType.TypeName}. Actual value: $right") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                            addNote(Consts.Logger.hintTitle,
                                    "The value returned by this expression must be a ${BitListType.TypeName}")
                        }
                    }
                }

                // Add both.
                val left = analyzer.memory.popStack() as LxmBitList

                val resultBitSet = BitSet(left.size + right.size)

                for (i in 0 until left.size) {
                    resultBitSet[i] = left.primitive[i]
                }

                for (i in 0 until right.size) {
                    resultBitSet[i + left.size] = right.primitive[i]
                }

                analyzer.memory.pushStack(LxmBitList(left.size + right.size, resultBitSet))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
