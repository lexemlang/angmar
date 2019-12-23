package org.lexem.angmar.compiler.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.descriptive.lexemes.*

/**
 * Compiler for [BinarySequenceLexemeNode].
 */
internal class BinarySequenceLexemeCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: BinarySequenceLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var isNegated = false
    var propertyPostfix: LexemePropertyPostfixCompiled? = null
    lateinit var bitlist: CompiledNode

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            BinarySequenceLexemAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: BinarySequenceLexemeNode): CompiledNode {
            val result = BinarySequenceLexemeCompiled(parent, parentSignal, node)
            result.isNegated = node.isNegated
            result.propertyPostfix =
                    node.propertyPostfix?.compile(result, BinarySequenceLexemAnalyzer.signalEndPropertyPostfix)
            result.bitlist = node.bitlist.compile(result, BinarySequenceLexemAnalyzer.signalEndBitlist)

            // Constant text.
            val bitlist = result.bitlist
            if (bitlist is ConstantCompiled) {
                val bitListValue = (bitlist.value as LxmBitList).primitive

                // Empty text lexeme.
                if (bitListValue.size == 0) {
                    return if (result.isNegated) {
                        InitBacktrackingCompiled(parent, parentSignal, node)
                    } else {
                        ConstantCompiled(parent, parentSignal, node, LxmBitList.Empty)
                    }
                }
            }

            return result
        }
    }
}
