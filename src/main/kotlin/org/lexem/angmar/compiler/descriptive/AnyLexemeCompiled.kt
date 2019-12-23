package org.lexem.angmar.compiler.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.descriptive.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [AnyLexemeNode].
 */
internal class AnyLexemeCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: AnyLexemeNode) : CompiledNode(parent, parentSignal, parserNode) {
    var dataCapturing: CompiledNode? = null
    lateinit var lexeme: CompiledNode
    var quantifier: CompiledNode? = null
    var isBlock = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = AnyLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: AnyLexemeNode): CompiledNode {
            val result = AnyLexemeCompiled(parent, parentSignal, node)
            result.dataCapturing = node.dataCapturing?.compile(result, AnyLexemeAnalyzer.signalEndDataCapturing)
            result.lexeme = node.lexeme.compile(result, AnyLexemeAnalyzer.signalEndLexem)
            result.quantifier = node.quantifier?.compile(result, AnyLexemeAnalyzer.signalEndQuantifier)
            result.isBlock = node.lexeme is BlockStmtNode

            val quantifier = result.quantifier
            val lexeme = result.lexeme
            if (quantifier is ConstantCompiled) {
                val qtf = quantifier.value as LxmQuantifier

                // Remove when quantifier is {0}.
                if (qtf.max == 0 && !qtf.isInfinite) {
                    return NoOperationCompiled(parent, parentSignal, node)
                }

                if (qtf.isInfinite && (lexeme is ConstantCompiled || lexeme is NoOperationCompiled)) {
                    throw AngmarCompilerException(AngmarCompilerExceptionType.InfiniteLoop,
                            "Cannot quantify to infinity a constant lexem.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.quantifier!!.from.position(), node.quantifier!!.to.position() - 1)
                            message = "Remove the quantifier"
                        }
                    }
                }
            }

            // If lexeme is constant and there is no data capturing or quantifier return itself.
            if (quantifier == null && result.dataCapturing == null) {
                when (lexeme) {
                    is ConstantCompiled -> {
                        return NoOperationCompiled(parent, parentSignal, node)
                    }
                    is NoOperationCompiled -> {
                        return lexeme.linkTo(parent, parentSignal, node)
                    }
                    is InitBacktrackingCompiled -> {
                        return lexeme.linkTo(parent, parentSignal, node)
                    }
                }
            }

            return result
        }
    }
}
