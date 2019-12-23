package org.lexem.angmar.compiler.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*
import java.util.*

/**
 * Compiler for [BitlistNode].
 */
internal class BitlistCompiled private constructor(parent: CompiledNode?, parentSignal: Int, parserNode: BitlistNode) :
        CompiledNode(parent, parentSignal, parserNode) {
    val elements = mutableListOf<CompiledNode>()

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = BitListAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: BitlistNode): CompiledNode {
            val result = BitlistCompiled(parent, parentSignal, node)

            var last: BitList? = null
            for (escape in node.elements) {
                val compiledEscape =
                        escape.compile(result, BitListAnalyzer.signalEndFirstElement + result.elements.size)

                if (compiledEscape is ConstantCompiled) {
                    val primitive = compiledEscape.value as? LxmBitList ?: throw AngmarCompilerException(
                            AngmarCompilerExceptionType.IncompatibleType,
                            "${BitListType.TypeName} literals require that internal escaped expressions return a ${BitListType.TypeName}.") {
                        val fullText = compiledEscape.parser.reader.readAllText()
                        addSourceCode(fullText, compiledEscape.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(compiledEscape.from.position(), compiledEscape.to.position() - 1)
                            addNote(Consts.Logger.hintTitle,
                                    "The value returned by this expression must be a ${BitListType.TypeName}")
                        }
                    }

                    last = if (last == null) {
                        primitive.primitive
                    } else {
                        addTwoBitLists(last, primitive.primitive)
                    }
                } else {
                    result.elements.add(compiledEscape)
                    last = null
                }
            }

            if (result.elements.isEmpty()) {
                return if (last != null) {
                    ConstantCompiled(parent, parentSignal, node, LxmBitList(last))
                } else {
                    ConstantCompiled(parent, parentSignal, node, LxmBitList.Empty)
                }
            }

            // Add the last value.
            if (last != null) {
                val finalValue =
                        ConstantCompiled(result, BitListAnalyzer.signalEndFirstElement + result.elements.size, node,
                                LxmBitList(last))
                result.elements.add(finalValue)
            }

            return result
        }

        /**
         * Adds two [BitList].
         */
        fun addTwoBitLists(left: BitList, right: BitList): BitList {
            val resultBitSet = BitSet(left.size + right.size)

            for (i in 0 until left.size) {
                resultBitSet[i] = left[i]
            }

            for (i in 0 until right.size) {
                resultBitSet[i + left.size] = right[i]
            }

            return BitList(left.size + right.size, resultBitSet)
        }
    }
}
