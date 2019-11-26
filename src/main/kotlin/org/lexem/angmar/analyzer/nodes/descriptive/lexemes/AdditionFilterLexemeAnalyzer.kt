package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
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
                val lxmNodeRef = analyzer.memory.getLastFromStack()

                if (node.nextAccess != null) {
                    // Save the result as the node to re-parse or filter.
                    val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                    context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenNode2Filter, lxmNodeRef)

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    return analyzer.nextNode(node.nextAccess)
                }

                finalizeNode(analyzer, lxmNodeRef)
            }
            signalEndNextAccess -> {
                val resultRef = analyzer.memory.getLastFromStack()
                val result = resultRef.dereference(analyzer.memory)

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

                finalizeNode(analyzer, resultRef)

                // Remove HiddenNode2Filter.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.removeProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenNode2Filter)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Set the parent relationship of the node.
     */
    private fun finalizeNode(analyzer: LexemAnalyzer, lxmNodeRef: LexemPrimitive) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val nodePosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition) as LxmInteger
        val lxmNode = lxmNodeRef.dereference(analyzer.memory) as LxmNode
        val parentRef = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Node) as LxmReference
        val parent = parentRef.dereferenceAs<LxmNode>(analyzer.memory)!!
        val parentChildren = parent.getChildren(analyzer.memory)

        parentChildren.insertCell(analyzer.memory, nodePosition.primitive, lxmNodeRef, ignoreConstant = true)
        lxmNode.setParent(analyzer.memory, parentRef)

        // Update the node position.
        val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)
        val reverse = RelationalFunctions.isTruthy(
                props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Reverse) ?: LxmNil)
        if (!reverse) {
            analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.FilterNodePosition,
                    LxmInteger.from(nodePosition.primitive + 1))
        }
    }
}
