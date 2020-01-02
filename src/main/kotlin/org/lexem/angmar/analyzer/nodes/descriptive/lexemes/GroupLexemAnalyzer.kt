package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.descriptive.lexemes.*


/**
 * Analyzer for group lexemes.
 */
internal object GroupLexemAnalyzer {
    const val signalEndHeader = AnalyzerNodesCommons.signalStart + 1
    const val signalEndBadPatternContent = signalEndHeader + 1
    const val signalEndFirstPattern = signalEndBadPatternContent + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: GroupLexemeCompiled) {
        val signalBadPattern = signalEndFirstPattern + node.patterns.size + 1

        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Save the first index for atomic.
                val memoryIndex = LxmBigNode(analyzer.memory.lastNode)

                if (node.isNegated) {
                    // On backwards skip.
                    analyzer.freezeMemoryCopy(node, signalEndBadPatternContent)
                }

                // Put the index in the stack.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LastNode, memoryIndex)

                // Put the filter position in the stack.
                if (node.isFilterCode) {
                    val filterPosition = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SavedFilterNodePosition, filterPosition)
                }

                // Generate an intermediate context that will be removed at the end.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                AnalyzerCommons.createAndAssignNewFunctionContext(analyzer.memory, context, "Group", context.type)

                if (node.header != null) {
                    return analyzer.nextNode(node.header)
                } else {
                    // Set the quantifier.
                    val patternUnion =
                            LxmPatternUnion(analyzer.memory, LxmQuantifier.AlternativePattern, LxmInteger.Num0)
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, patternUnion)

                    // Create the node.
                    GroupHeaderLexemAnalyzer.createNode(analyzer, "", node.isFilterCode)
                }

                return analyzer.nextNode(node, signalEndHeader)
            }
            signalEndHeader -> {
                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = false) as LxmPatternUnion

                if (union.canHaveANextPattern(analyzer.memory)) {
                    val pattern = node.patterns.first()

                    // On backwards try next.
                    analyzer.freezeMemoryCopy(node, signalBadPattern)

                    return analyzer.nextNode(pattern)
                }

                return if (union.isFinished(analyzer.memory)) {
                    finish(analyzer, node)
                } else {
                    analyzer.initBacktracking()
                }
            }
            in signalEndFirstPattern..signalEndFirstPattern + node.patterns.size -> {
                val position = (signal - signalEndFirstPattern) + 1

                // Increase index.
                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = true) as LxmPatternUnion
                union.increaseIndex(analyzer.memory)

                if (union.canHaveANextPattern(analyzer.memory)) {
                    if (position < node.patterns.size) {
                        val pattern = node.patterns[position]

                        // On backwards try next.
                        analyzer.freezeMemoryCopy(node, signalBadPattern + position)

                        return analyzer.nextNode(pattern)
                    }
                }

                return if (union.isFinished(analyzer.memory)) {
                    finish(analyzer, node)
                } else {
                    analyzer.initBacktracking()
                }
            }
            in signalBadPattern..signalBadPattern + node.patterns.size -> {
                val position = (signal - signalBadPattern) + 1

                val union = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LexemeUnion).dereference(
                        analyzer.memory, toWrite = false) as LxmPatternUnion

                // Check the quantifier can be matched with the remaining patterns.
                if (!union.canFinish(analyzer.memory, node.patterns.size - position)) {
                    return analyzer.initBacktracking()
                }

                if (union.canHaveANextPattern(analyzer.memory)) {
                    if (position < node.patterns.size) {
                        val pattern = node.patterns[position]

                        // On backwards try next.
                        analyzer.freezeMemoryCopy(node, signalBadPattern + position)

                        return analyzer.nextNode(pattern)
                    }
                }

                return if (union.isFinished(analyzer.memory)) {
                    finish(analyzer, node)
                } else {
                    analyzer.initBacktracking()
                }
            }
            signalEndBadPatternContent -> {
                // Skip
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Process the finalization of the node.
     */
    private fun finish(analyzer: LexemAnalyzer, node: GroupLexemeCompiled) {
        // Check negation.
        if (node.isNegated) {
            val lastNode = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode
            analyzer.memory.collapseTo(lastNode.node)

            return analyzer.initBacktracking()
        }

        // Remove LexemeUnion and LastNode from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LexemeUnion)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LastNode)

        // Finish the node.
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
        val lxmNode = context.getDereferencedProperty<LxmNode>(analyzer.memory, AnalyzerCommons.Identifiers.Node,
                toWrite = true)!!
        lxmNode.setTo(analyzer.memory, analyzer.text.saveCursor())

        // Process the properties.
        let {
            val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = false)

            val children = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Children) ?: LxmNil)
            if (!children) {
                lxmNode.clearChildren(analyzer.memory)
            }

            var returnValue: LexemMemoryValue = if (lxmNode.name.isBlank()) {
                LxmNil
            } else {
                lxmNode
            }
            val capture = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Capture) ?: LxmNil)
            if (!capture && children) {
                // Set the returned value.
                val childrenList = lxmNode.getChildren(analyzer.memory, toWrite = false)
                returnValue = if (children && childrenList.size > 0) {
                    // Set the children as returned value.
                    val resultList = LxmList(analyzer.memory)
                    resultList.addCell(analyzer.memory, *childrenList.getAllCells().toList().toTypedArray())
                    resultList
                } else {
                    // Set a null value.
                    LxmNil
                }

                // Replace the node in parent by its children.
                lxmNode.replaceNodeInParentByChildren(analyzer.memory)
            }

            // Set the returned value.
            analyzer.memory.addToStackAsLast(returnValue)

            val propertyValue = props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Property) ?: LxmNil
            val property = RelationalFunctions.isTruthy(propertyValue)
            if (property) {
                val parent = lxmNode.getParent(analyzer.memory, toWrite = false)!!
                val parentProps = parent.getProperties(analyzer.memory, toWrite = true)

                if (propertyValue is LxmString) {
                    val value = lxmNode.getContent(analyzer.memory)!!
                    parentProps.setProperty(analyzer.memory, propertyValue.primitive, value)
                } else {
                    val value = lxmNode.getContent(analyzer.memory)!!
                    parentProps.setProperty(analyzer.memory, lxmNode.name, value)
                }
            }

            val backtrack = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Backtrack) ?: LxmNil)
            if (!backtrack) {
                val lastNode = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode
                analyzer.memory.collapseTo(lastNode.node)
            }

            val consume = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Consume) ?: LxmNil)
            if (!consume) {
                if (node.isFilterCode) {
                    val filterPosition =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SavedFilterNodePosition)
                    analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.FilterNodePosition, filterPosition)
                } else {
                    lxmNode.getFrom(analyzer.memory).primitive.restore()
                }
            }

            if (node.isFilterCode) {
                // Remove SavedFilterNodePosition from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SavedFilterNodePosition)
            }
        }
        // Set parent node as the current one.
        analyzer.setUpperNode()

        // Remove the reference.
        AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
