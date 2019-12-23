package org.lexem.angmar.compiler.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.loops.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.loops.*

/**
 * Compiler for [ConditionalLoopStmtNode].
 */
internal class ConditionalLoopStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: ConditionalLoopStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var condition: CompiledNode
    lateinit var thenBlock: CompiledNode
    var index: CompiledNode? = null
    var lastClauses: LoopClausesStmtCompiled? = null
    var isUntil = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ConditionalLoopStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ConditionalLoopStmtNode): CompiledNode {
            val result = ConditionalLoopStmtCompiled(parent, parentSignal, node)
            result.condition = node.condition.compile(result, ConditionalLoopStmtAnalyzer.signalEndCondition)
            result.thenBlock = node.thenBlock.compile(result, ConditionalLoopStmtAnalyzer.signalEndThenBlock)
            result.index = node.index?.compile(result, ConditionalLoopStmtAnalyzer.signalEndIndex)
            result.lastClauses = node.lastClauses?.compile(result, ConditionalLoopStmtAnalyzer.signalEndLastClause)
            result.isUntil = node.isUntil

            val condition = result.condition
            if (condition is ConstantCompiled) {
                var conditionAsTruthy = RelationalFunctions.isTruthy(condition.value)

                if (result.isUntil) {
                    conditionAsTruthy = !conditionAsTruthy
                }

                if (conditionAsTruthy) {
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

                    // The last clauses will never be executed.
                    result.lastClauses = null
                } else {
                    if (result.lastClauses?.elseBlock == null) {
                        return NoOperationCompiled(parent, parentSignal, node)
                    }
                }
            }

            return result
        }
    }
}
