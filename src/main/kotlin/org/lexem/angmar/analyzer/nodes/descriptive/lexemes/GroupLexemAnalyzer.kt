package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*


/**
 * Analyzer for group lexemes.
 */
internal object GroupLexemAnalyzer {
    const val signalEndHeader = AnalyzerNodesCommons.signalStart + 1
    const val signalEndBadPatternContent = signalEndHeader + 1
    const val signalEndFirstPattern = signalEndBadPatternContent + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: GroupLexemeNode) {
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
                val contextRef = AnalyzerCommons.getCurrentContextReference(analyzer.memory)
                AnalyzerCommons.createAndAssignNewFunctionContext(analyzer.memory, contextRef, "Group")

                if (node.header != null) {
                    return analyzer.nextNode(node.header)
                } else {
                    // Set the quantifier.
                    val patternUnion =
                            LxmPatternUnion(LxmQuantifier.AlternativePattern, LxmInteger.Num0, analyzer.memory)
                    val patternUnionRef = analyzer.memory.add(patternUnion)
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LexemeUnion, patternUnionRef)

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
            in signalEndFirstPattern until signalEndFirstPattern + node.patterns.size -> {
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
            in signalBadPattern until signalBadPattern + node.patterns.size -> {
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
    private fun finish(analyzer: LexemAnalyzer, node: GroupLexemeNode) {
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
        val lxmNodeRef = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Node) as LxmReference
        val lxmNode = lxmNodeRef.dereferenceAs<LxmNode>(analyzer.memory, toWrite = true)!!
        lxmNode.setTo(analyzer.memory, analyzer.text.saveCursor())

        // Process the properties.
        let {
            val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory, toWrite = false)

            val children = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Children) ?: LxmNil)
            if (!children) {
                val childList = lxmNode.getChildren(analyzer.memory, toWrite = true)
                childList.removeCell(analyzer.memory, childList.actualListSize - 1, ignoreConstant = true)
            }

            var returnValue: LexemPrimitive = if (lxmNode.name.isBlank()) {
                LxmNil
            } else {
                lxmNodeRef
            }
            val capture = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Capture) ?: LxmNil)
            if (!capture) {
                val parent = lxmNode.getParent(analyzer.memory, toWrite = false)!!
                val parentChildren = parent.getChildren(analyzer.memory, toWrite = true)

                // Find position in parent.
                var index = -1
                for ((i, value) in parentChildren.getAllCells().withIndex().reversed()) {
                    if (value == lxmNodeRef) {
                        index = i
                        break
                    }
                }

                if (index == -1) {
                    throw AngmarUnreachableException()
                }

                // Remove from parent.
                parentChildren.removeCell(analyzer.memory, index, ignoreConstant = true)

                // Add children to parent in the same position.
                val childrenArray = lxmNode.getChildren(analyzer.memory, toWrite = true).getAllCells().toTypedArray()
                parentChildren.insertCell(analyzer.memory, index, *childrenArray, ignoreConstant = true)

                // Set the returned value.
                if (children && childrenArray.isNotEmpty()) {
                    // Set the children as returned value.
                    val resultList = LxmList(analyzer.memory)
                    val resultListRef = analyzer.memory.add(resultList)
                    resultList.addCell(analyzer.memory, *childrenArray)
                    returnValue = resultListRef
                } else {
                    // Set a null value.
                    returnValue = LxmNil
                }
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
