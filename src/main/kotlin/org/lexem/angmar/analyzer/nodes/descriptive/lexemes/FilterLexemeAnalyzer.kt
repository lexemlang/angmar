package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for filter selectors.
 */
internal object FilterLexemeAnalyzer {
    const val signalEndSelector = AnalyzerNodesCommons.signalStart + 1
    const val signalEndNextAccess = signalEndSelector + 1
    private const val signalBadEnd = signalEndNextAccess + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FilterLexemeCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = false)
                val reverse = RelationalFunctions.isTruthy(
                        props.getPropertyValue(AnalyzerCommons.Properties.Reverse) ?: LxmNil)

                val nodePosition =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger
                val actualPosition = if (reverse) {
                    nodePosition.primitive - 1
                } else {
                    nodePosition.primitive
                }
                val lxmNode = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNode).dereference(
                        analyzer.memory, toWrite = false) as LxmNode
                val children = lxmNode.getChildren(toWrite = false)
                val node2Process = children.getCell(actualPosition)

                if (node2Process != null) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, node2Process)

                    if (node.isNegated) {
                        // Save the first index for atomic.
                        val memoryIndex = LxmBigNode(analyzer.memory.lastNode)

                        // On backwards skip.
                        analyzer.freezeMemoryCopy(node, signalBadEnd)

                        // Put the index in the stack.
                        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LastNode, memoryIndex)
                    }

                    return analyzer.nextNode(node.selector)
                }

                if (node.isNegated) {
                    analyzer.memory.addToStackAsLast(LxmNil)
                } else {
                    return analyzer.initBacktracking()
                }
            }
            signalEndSelector -> {
                val condition = analyzer.memory.getLastFromStack() as LxmLogic

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (condition.primitive) {
                    val lxmNodeRef = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node)

                    if (node.nextAccess != null) {
                        // Save the result as the node to re-parse or filter.
                        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                        context.setProperty(AnalyzerCommons.Identifiers.HiddenNode2Filter, lxmNodeRef)

                        // Remove Node from the stack.
                        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)

                        return analyzer.nextNode(node.nextAccess)
                    }

                    val lxmNode = lxmNodeRef.dereference(analyzer.memory, toWrite = true) as LxmNode

                    finalizeNode(analyzer, lxmNode)

                    if (node.isNegated) {
                        // Restore the memory index of the start and continue.
                        val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                        analyzer.restoreMemoryCopy(index.node)

                        return analyzer.initBacktracking()
                    }

                    analyzer.memory.addToStackAsLast(lxmNode)
                } else {
                    if (!node.isNegated) {
                        return analyzer.initBacktracking()
                    }

                    // Restore the memory index of the start and continue.
                    val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                    analyzer.restoreMemoryCopy(index.node)

                    analyzer.memory.addToStackAsLast(LxmNil)
                }

                // Remove Node from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
            }
            signalEndNextAccess -> {
                val result = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = true)

                if (result !is LxmNode) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.FilterLexemWithNextRequiresANode,
                            "The next accesses of a Filter lexeme require the final result be a node.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.nextAccess!!.from.position(), node.nextAccess!!.to.position() - 1)
                            message = "Review that the returned value of this expression is a node"
                        }
                    }
                }

                finalizeNode(analyzer, result)

                if (node.isNegated) {
                    // Restore the memory index of the start and continue.
                    val index = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

                    analyzer.restoreMemoryCopy(index.node)

                    return analyzer.initBacktracking()
                }

                // Remove HiddenNode2Filter.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                context.removeProperty(AnalyzerCommons.Identifiers.HiddenNode2Filter)
            }
            signalBadEnd -> {
                // Skip
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Set the parent relationship of the node.
     */
    private fun finalizeNode(analyzer: LexemAnalyzer, lxmNode: LxmNode) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val parent = context.getDereferencedProperty<LxmNode>(AnalyzerCommons.Identifiers.Node, toWrite = false)!!

        // Link to the new parent.
        lxmNode.addToParent(parent)

        // Update the node position.
        updatePosition(analyzer)
    }

    /**
     * Updates the position of the filter.
     */
    private fun updatePosition(analyzer: LexemAnalyzer) {
        val nodePosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger

        // Update the node position.
        val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = false)
        val reverse = RelationalFunctions.isTruthy(props.getPropertyValue(AnalyzerCommons.Properties.Reverse) ?: LxmNil)
        if (reverse) {
            analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.FilterNodePosition,
                    LxmInteger.from(nodePosition.primitive - 1))
        } else {
            analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.FilterNodePosition,
                    LxmInteger.from(nodePosition.primitive + 1))
        }
    }
}
