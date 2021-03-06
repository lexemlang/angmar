package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.data.referenced.iterators.*
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

                // Save the index.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LoopIndexValue, LxmInteger.Num0)

                if (node.index != null) {
                    return analyzer.nextNode(node.index)
                }

                return analyzer.nextNode(node.variable)
            }
            signalEndIndex -> {
                // Save the index.
                val indexName = analyzer.memory.getLastFromStack() as LxmString
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.LoopIndexName)

                // Set the index in the context.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, indexName.primitive, LxmInteger.Num0)

                return analyzer.nextNode(node.variable)
            }
            signalEndVariable -> {
                // Check identifier if it is not a destructuring.
                if (node.variable !is DestructuringStmtNode) {
                    val variable = analyzer.memory.getLastFromStack()

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

                    // Move Last to LoopVariable in the stack.
                    analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.LoopVariable)
                }

                return analyzer.nextNode(node.condition)
            }
            signalEndCondition -> {
                // Create the iterator.
                val valueRef = analyzer.memory.getLastFromStack()
                val value = valueRef.dereference(analyzer.memory)

                val iterator = AnalyzerIteratorCommons.createIterator(analyzer.memory, valueRef)
                        ?: throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                                "The iterator loop cannot iterate over the returned value by the expression. Actual value: $value") {
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
                val iteratorRef = analyzer.memory.add(iterator)

                // Save iterator.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LoopIterator, iteratorRef)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                return advanceIterator(analyzer, node, advance = false)
            }
            signalEndThenBlock -> {
                return advanceIterator(analyzer, node)
            }
            signalEndLastClause -> {
                finish(analyzer, node)
            }
            // Process the control signals.
            AnalyzerNodesCommons.signalExitControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                finish(analyzer, node)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)
            }
            AnalyzerNodesCommons.signalNextControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    finish(analyzer, node)

                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                return advanceIterator(analyzer, node)
            }
            AnalyzerNodesCommons.signalRedoControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    finish(analyzer, node)

                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                return advanceIterator(analyzer, node, advance = false)
            }
            AnalyzerNodesCommons.signalRestartControl -> {
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl
                val contextTag = AnalyzerCommons.getCurrentContextTag(analyzer.memory)

                // Propagate the control signal.
                if (control.tag != null && control.tag != contextTag) {
                    finish(analyzer, node)

                    return analyzer.nextNode(node.parent, signal)
                }

                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                // Start again the loop.
                val iteratorRef = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIterator)
                val iterator = iteratorRef.dereference(analyzer.memory) as LexemIterator
                iterator.restart(analyzer.memory)

                analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.LoopIndexValue, LxmInteger.Num0)

                return advanceIterator(analyzer, node, advance = false)
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalReturnControl -> {
                finish(analyzer, node)

                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Increment the iteration index.
     */
    private fun incrementIterationIndex(analyzer: LexemAnalyzer, node: IteratorLoopStmtNode, count: Int = 1) {
        val lastIndex = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexValue) as LxmInteger
        val newIndex = LxmInteger.from(lastIndex.primitive + count)

        analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.LoopIndexValue, newIndex)

        // Set the index if there is an index expression.
        if (node.index != null) {
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
            val indexName = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexName) as LxmString

            context.setProperty(analyzer.memory, indexName.primitive, newIndex)
        }
    }

    /**
     * Advances the iterator assigning the next value.
     */
    private fun advanceIterator(analyzer: LexemAnalyzer, node: IteratorLoopStmtNode, advance: Boolean = true) {
        val iteratorRef = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIterator)
        val iterator = iteratorRef.dereference(analyzer.memory) as LexemIterator
        val variable = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopVariable)
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)

        if (advance) {
            iterator.advance(analyzer.memory)

            // Increase the current index.
            incrementIterationIndex(analyzer, node)
        }

        if (iterator.isEnded(analyzer.memory)) {
            val indexValue = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexValue) as LxmInteger

            if (indexValue.primitive == 0 && node.lastClauses?.elseBlock != null) {
                return analyzer.nextNode(node.lastClauses!!.elseBlock)
            }

            if (indexValue.primitive != 0 && node.lastClauses?.lastBlock != null) {
                // Remove the name of the intermediate statement.
                context.removeProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag)

                return analyzer.nextNode(node.lastClauses!!.lastBlock)
            }

            finish(analyzer, node)

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

    /**
     * Process the finalization of the loop.
     */
    private fun finish(analyzer: LexemAnalyzer, node: IteratorLoopStmtNode) {
        // Remove the intermediate context.
        AnalyzerCommons.removeCurrentContextAndAssignPrevious(analyzer.memory)

        // Remove LoopIndexName, LoopVariable, LoopIterator and LoopIndexValue from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopIndexValue)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopVariable)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopIterator)
        if (node.index != null) {
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LoopIndexName)
        }
    }
}
