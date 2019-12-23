package org.lexem.angmar.compiler.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.functional.statements.*

/**
 * Compiler for [DestructuringStmtNode].
 */
internal class DestructuringStmtCompiled private constructor(parent: CompiledNode?, parentSignal: Int,
        parserNode: DestructuringStmtNode) : CompiledNode(parent, parentSignal, parserNode) {
    var alias: CompiledNode? = null
    val elements = mutableListOf<DestructuringElementStmtCompiled>()
    var spread: DestructuringSpreadStmtCompiled? = null
    lateinit var initialValue: LxmDestructuring

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            DestructuringStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        fun compile(parent: CompiledNode, parentSignal: Int, node: DestructuringStmtNode): DestructuringStmtCompiled {
            val result = DestructuringStmtCompiled(parent, parentSignal, node)
            val alias = node.alias?.compile(result, DestructuringStmtAnalyzer.signalEndAlias)
            val spread = node.spread?.compile(result, DestructuringStmtAnalyzer.signalEndSpread)

            val initial = LxmDestructuring()
            result.initialValue = initial

            if (alias is ConstantCompiled) {
                initial.setAlias((alias.value as LxmString).primitive)
            } else {
                result.alias = alias
            }

            if (spread != null) {
                val spreadIdentifier = spread.identifier
                if (spreadIdentifier is ConstantCompiled) {
                    initial.setSpread((spreadIdentifier.value as LxmString).primitive, spread.isConstant)
                } else {
                    result.spread = spread
                }
            }

            var continueAdding = true
            for (element in node.elements) {
                val compiledElement =
                        element.compile(result, result.elements.size + DestructuringStmtAnalyzer.signalEndFirstElement)

                if (continueAdding) {
                    val alias = compiledElement.alias
                    val original = compiledElement.original
                    if (alias is ConstantCompiled && (original == null || original is ConstantCompiled)) {
                        if (original == null) {
                            initial.addElement((alias.value as LxmString).primitive, compiledElement.isConstant)
                        } else {
                            original as ConstantCompiled

                            initial.addElement((original.value as LxmString).primitive,
                                    (alias.value as LxmString).primitive, compiledElement.isConstant)
                        }
                    } else {
                        result.elements.add(compiledElement)
                        continueAdding = false
                    }
                } else {
                    result.elements.add(compiledElement)
                }
            }

            return result
        }
    }
}
