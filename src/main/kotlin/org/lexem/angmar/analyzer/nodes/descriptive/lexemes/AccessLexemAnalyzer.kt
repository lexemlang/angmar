package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for access lexemes.
 */
internal object AccessLexemAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1
    const val signalEndNextAccess = signalEndExpression + 1
    const val signalBadEndExpression = signalEndNextAccess + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AccessLexemeCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.isNegated) {
                    // Save the first index for atomic.
                    val memoryIndex = LxmBigNode(analyzer.memory.lastNode)

                    // On backwards skip.
                    analyzer.freezeMemoryCopy(node, signalBadEndExpression)

                    // Put the index in the stack.
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LastNode, memoryIndex)
                }

                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                if (node.isNegated) {
                    val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                    analyzer.restoreMemoryCopy(index.node)

                    return analyzer.initBacktracking()
                }

                if (node.nextAccess != null) {
                    val resultRef = analyzer.memory.getLastFromStack()
                    val result = resultRef.dereference(analyzer.memory, toWrite = false)

                    if (result !is LxmNode) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.AccessLexemWithNextRequiresANode,
                                "The next access of an access lexeme require that the result of the previous access returns a node.") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                            addSourceCode(fullText) {
                                title = Consts.Logger.hintTitle
                                highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                                message = "Review that the returned value of this expression is a node"
                            }
                        }
                    }

                    // Save the result as the node to re-parse or filter.
                    val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                    context.setProperty(AnalyzerCommons.Identifiers.HiddenNode2Filter, resultRef)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    return analyzer.nextNode(node.nextAccess)
                }
            }
            signalEndNextAccess -> {
                // Remove HiddenNode2Filter.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                context.removeProperty(AnalyzerCommons.Identifiers.HiddenNode2Filter)
            }
            signalBadEndExpression -> {
                // Return nil as default value.
                analyzer.memory.addToStackAsLast(LxmNil)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
