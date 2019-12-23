package org.lexem.angmar.compiler.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.loops.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.loops.*

/**
 * Compiler for [InfiniteLoopStmtNode].
 */
internal class InfiniteLoopStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: InfiniteLoopStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var thenBlock: CompiledNode
    var index: CompiledNode? = null

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            InfiniteLoopStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: InfiniteLoopStmtNode): CompiledNode {
            val result = InfiniteLoopStmtCompiled(parent, parentSignal, node)
            result.thenBlock = node.thenBlock.compile(result, InfiniteLoopStmtAnalyzer.signalEndThenBlock)
            result.index = node.index?.compile(result, InfiniteLoopStmtAnalyzer.signalEndIndex)

            if (result.thenBlock is NoOperationCompiled) {
                throw AngmarCompilerException(AngmarCompilerExceptionType.InfiniteLoop, "This loop never end") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                        addNote(Consts.Logger.hintTitle, "Add a condition to eventually stop it")
                    }
                }
            }

            return result
        }
    }
}
