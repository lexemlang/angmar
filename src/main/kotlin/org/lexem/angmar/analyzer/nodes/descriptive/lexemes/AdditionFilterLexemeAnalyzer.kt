package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*


/**
 * Analyzer for addition filter lexemes.
 */
internal object AdditionFilterLexemeAnalyzer {
    const val signalEndSelector = AnalyzerNodesCommons.signalStart + 1
    const val signalEndNextAccess = signalEndSelector + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AdditionFilterLexemeNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.selector)
            }
            signalEndSelector -> {
                if (node.nextAccess != null) {
                    val lxmNodeRef = analyzer.memory.getLastFromStack()

                    // Save the result as the node to re-parse or filter.
                    val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                    context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenNode2Filter, lxmNodeRef)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    return analyzer.nextNode(node.nextAccess)
                } else {
                    val result =
                            analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = true) as LxmNode

                    finalizeNode(analyzer, result)
                }
            }
            signalEndNextAccess -> {
                val result = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = true)

                if (result !is LxmNode) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.AdditionLexemWithNextRequiresANode,
                            "The next accesses of an Addition lexeme require the final result be a node.") {
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

                // Remove HiddenNode2Filter.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                context.removeProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenNode2Filter)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Set the parent relationship of the node.
     */
    private fun finalizeNode(analyzer: LexemAnalyzer, lxmNode: LxmNode) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val nodePosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger
        val parent = context.getDereferencedProperty<LxmNode>(analyzer.memory, AnalyzerCommons.Identifiers.Node,
                toWrite = false)!!
        val parentChildren = parent.getChildren(analyzer.memory, toWrite = true)

        parentChildren.insertCell(analyzer.memory, nodePosition.primitive, lxmNode, ignoreConstant = true)
        lxmNode.setParent(analyzer.memory, parent)

        // Update the node position.
        val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = false)
        val reverse = RelationalFunctions.isTruthy(
                props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Reverse) ?: LxmNil)
        if (!reverse) {
            analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.FilterNodePosition,
                    LxmInteger.from(nodePosition.primitive + 1))
        }
    }
}
