package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.primitives.iterators.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.loops.*


/**
 * Analyzer for iterator loop statements.
 */
internal object IteratorLoopStmtAnalyzer {
    const val signalEndIndex = AnalyzerNodesCommons.signalStart + 1
    const val signalEndVariable = signalEndIndex + 1
    const val signalEndCondition = signalEndVariable + 1
    const val signalEndThenBlock = signalEndCondition + 1
    const val signalEndLastClause = signalEndThenBlock + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: IteratorLoopStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory)

                // Save the index
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexValue, LxmInteger.Num0)

                if (node.index != null) {
                    return analyzer.nextNode(node.index)
                }

                return analyzer.nextNode(node.variable)
            }
            signalEndIndex -> {
                // Save the index
                val indexName = analyzer.memory.popStack() as LxmString
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)

                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexName, indexName)
                context.setProperty(analyzer.memory, indexName.primitive, LxmInteger.Num0)

                return analyzer.nextNode(node.variable)
            }
            signalEndVariable -> {
                // Check identifier if it is not a destructuring.
                if (node.variable !is DestructuringStmtNode) {
                    val variable = analyzer.memory.popStack()

                    if (variable !is LxmString) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                                "The returned value by the variable expression must be a ${StringType.TypeName}. Actual value: $variable") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                            addSourceCode(fullText) {
                                title = Consts.Logger.hintTitle
                                highlightSection(node.variable.from.position(), node.variable.to.position() - 1)
                                message = "Review the returned value of this expression"
                            }
                        }
                    }

                    val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                    context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopVariable, variable)
                }

                return analyzer.nextNode(node.condition)
            }
            signalEndCondition -> {
                // Create the iterator.
                val value = analyzer.memory.popStack()
                val derefValue = value.dereference(analyzer.memory)

                val iterator =
                        AnalyzerIteratorCommons.createIterator(analyzer.memory, value) ?: throw AngmarAnalyzerException(
                                AngmarAnalyzerExceptionType.IncompatibleType,
                                "The fo loops cannot iterate over the returned value by the expression. Actual value: $derefValue") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                            addSourceCode(fullText) {
                                title = Consts.Logger.hintTitle
                                highlightSection(node.condition.from.position(), node.condition.to.position() - 1)
                                message = "Review the returned value of this expression"
                            }
                        }

                // Decrement the reference count.
                if (value is LxmReference) {
                    value.decreaseReferenceCount(analyzer.memory)
                }

                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIterator, iterator)

                return advanceIterator(analyzer, node, advance = false)
            }
            signalEndThenBlock -> {
                return advanceIterator(analyzer, node)
            }
            signalEndLastClause -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)
            }
            // Process the control signals.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }
            }
            AnalyzerNodesCommons.signalNextControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    // Remove the iteration context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }

                return advanceIterator(analyzer, node)
            }
            AnalyzerNodesCommons.signalRedoControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    // Remove the iteration context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }

                return advanceIterator(analyzer, node, advance = false)
            }
            AnalyzerNodesCommons.signalRestartControl -> {
                val control = analyzer.memory.popStack() as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    // Remove the iteration context.
                    AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                    analyzer.memory.pushStack(control)
                    return analyzer.nextNode(node.parent, signal)
                }

                // Start again the loop.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val iterator = context.getPropertyValue(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenLoopIterator) as LexemIterator
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexValue, LxmInteger.Num0)
                iterator.restart(analyzer.memory)

                return advanceIterator(analyzer, node, advance = false)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalReturnControl -> {
                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Increment the iteration index.
     */
    private fun incrementIterationIndex(analyzer: LexemAnalyzer, node: IteratorLoopStmtNode, count: Int = 1) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val lastIndex = context.getDereferencedProperty<LxmInteger>(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenLoopIndexValue)!!
        val newIndex = LxmInteger.from(lastIndex.primitive + count)

        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenLoopIndexValue, newIndex)

        // Set the index if there is an index expression.
        if (node.index != null) {
            val indexName = context.getDereferencedProperty<LxmString>(analyzer.memory,
                    AnalyzerCommons.Identifiers.HiddenLoopIndexName)!!

            context.setProperty(analyzer.memory, indexName.primitive, newIndex)
        }
    }

    /**
     * Advances the iterator assigning the next value.
     */
    private fun advanceIterator(analyzer: LexemAnalyzer, node: IteratorLoopStmtNode, advance: Boolean = true) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
        val iterator = context.getPropertyValue(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenLoopIterator) as LexemIterator
        val variable = context.getPropertyValue(analyzer.memory,
                AnalyzerCommons.Identifiers.HiddenLoopVariable) as LexemPrimitive

        if (advance) {
            iterator.advance(analyzer.memory)

            // Increase the current index.
            incrementIterationIndex(analyzer, node)
        }

        if (iterator.isEnded) {
            val indexValue = context.getDereferencedProperty<LxmInteger>(analyzer.memory,
                    AnalyzerCommons.Identifiers.HiddenLoopIndexValue)!!

            // Call the destroy over the iterator.
            iterator.finalize(analyzer.memory)

            if (indexValue.primitive == 0 && node.lastClauses?.elseBlock != null) {
                return analyzer.nextNode(node.lastClauses!!.elseBlock)
            }

            if (indexValue.primitive != 0 && node.lastClauses?.lastBlock != null) {
                // Remove the name of the intermediate statement.
                context.removeProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag)

                return analyzer.nextNode(node.lastClauses!!.lastBlock)
            }

            // Remove the iteration and intermediate contexts.
            AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

            return analyzer.nextNode(node.parent, node.parentSignal)
        }

        val iteratorValue = iterator.getCurrent(analyzer.memory)!!
        val value = if (iteratorValue.first == null) {
            iteratorValue.second
        } else {
            val obj = LxmObject()
            val objRef = analyzer.memory.add(obj)
            obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Key, iteratorValue.first!!)
            obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Value, iteratorValue.second)

            objRef
        }

        // Perform the destructuring.
        if (node.variable is DestructuringStmtNode) {
            variable as LxmDestructuring

            when (val derefValue = value.dereference(analyzer.memory)) {
                is LxmObject -> variable.destructureObject(analyzer.memory, derefValue, context)
                is LxmList -> variable.destructureList(analyzer.memory, derefValue, context)
                else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                        "Destructuring is only available for ${ObjectType.TypeName}s and ${ListType.TypeName}s. Actual value: $derefValue") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                    addSourceCode(fullText) {
                        title = Consts.Logger.hintTitle
                        highlightSection(node.condition.from.position(), node.condition.to.position() - 1)
                        message = "Review the returned value of this expression"
                    }
                }
            }
        } else {
            variable as LxmString

            context.setPropertyAsContext(analyzer.memory, variable.primitive, value)
        }

        return analyzer.nextNode(node.thenBlock)
    }
}
