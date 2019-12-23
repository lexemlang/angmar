package org.lexem.angmar.compiler.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.functional.statements.selective.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.functional.statements.selective.*

/**
 * Compiler for [ConditionalPatternSelectiveStmtNode].
 */
internal class ConditionalPatternSelectiveStmtCompiled(parent: CompiledNode?, parentSignal: Int,
        parserNode: ConditionalPatternSelectiveStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    lateinit var condition: CompiledNode
    var isUnless = false

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ConditionalPatternSelectiveStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: ConditionalPatternSelectiveStmtNode): CompiledNode {
            val result = ConditionalPatternSelectiveStmtCompiled(parent, parentSignal, node)
            result.condition =
                    node.condition.compile(result, ConditionalPatternSelectiveStmtAnalyzer.signalEndCondition)
            result.isUnless = node.isUnless

            val condition = result.condition
            if (condition is ConstantCompiled) {
                val primitive = condition.value
                var primitiveAsTruthy = RelationalFunctions.isTruthy(primitive)

                if (node.isUnless) {
                    primitiveAsTruthy = !primitiveAsTruthy
                }

                return ConstantCompiled(parent, parentSignal, node, LxmLogic.from(primitiveAsTruthy))
            }

            return result
        }
    }
}
