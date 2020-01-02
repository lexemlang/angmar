package org.lexem.angmar.analyzer.nodes.functional.statements.loops

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.data.referenced.iterators.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.functional.statements.loops.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


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

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: IteratorLoopStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                // Generate an intermediate context.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                AnalyzerCommons.createAndAssignNewContext(analyzer.memory, context.type)

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
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
                context.setProperty(analyzer.memory, indexName.primitive, LxmInteger.Num0)

                return analyzer.nextNode(node.variable)
            }
            signalEndVariable -> {
                // Check identifier if it is not a destructuring.
                if (node.mustBeIdentifier) {
                    val variable = analyzer.memory.getLastFromStack()

                    if (variable !is LxmString) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                                "The returned value by the variable expression must be a ${StringType.TypeName}.") {
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
                val value = analyzer.memory.getLastFromStack().dereference(analyzer.memory, toWrite = false)

                val iterator =
                        AnalyzerIteratorCommons.createIterator(analyzer.memory, value) ?: throw AngmarAnalyzerException(
                                AngmarAnalyzerExceptionType.IncompatibleType,
                                "The iterator loop cannot iterate over the returned value by the expression.") {
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

                // Save iterator.
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LoopIterator, iterator)

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
                val iterator = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIterator).dereference(
                        analyzer.memory, toWrite = true) as LexemIterator
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
    private fun incrementIterationIndex(analyzer: LexemAnalyzer, node: IteratorLoopStmtCompiled, count: Int = 1) {
        val lastIndex = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexValue) as LxmInteger
        val newIndex = LxmInteger.from(lastIndex.primitive + count)

        analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.LoopIndexValue, newIndex)

        // Set the index if there is an index expression.
        if (node.index != null) {
            val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)
            val indexName = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexName) as LxmString

            context.setProperty(analyzer.memory, indexName.primitive, newIndex)
        }
    }

    /**
     * Advances the iterator assigning the next value.
     */
    private fun advanceIterator(analyzer: LexemAnalyzer, node: IteratorLoopStmtCompiled, advance: Boolean = true) {
        val iterator =
                analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIterator).dereference(analyzer.memory,
                        toWrite = true) as LexemIterator
        val variable = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopVariable)
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)

        if (advance) {
            iterator.advance(analyzer.memory)

            // Increase the current index.
            incrementIterationIndex(analyzer, node)
        }

        if (iterator.isEnded(analyzer.memory)) {
            val indexValue = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LoopIndexValue) as LxmInteger

            if (node.lastClauses != null) {
                return if (indexValue.primitive == 0) {
                    analyzer.memory.addToStackAsLast(LoopClausesStmtAnalyzer.optionForElse)
                    analyzer.nextNode(node.lastClauses)
                } else {
                    // Remove the name of the intermediate statement.
                    context.removeProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenContextTag)

                    analyzer.memory.addToStackAsLast(LoopClausesStmtAnalyzer.optionForLast)
                    analyzer.nextNode(node.lastClauses)
                }
            }

            finish(analyzer, node)

            return analyzer.nextNode(node.parent, node.parentSignal)
        }

        val iteratorValue = iterator.getCurrent(analyzer.memory)!!
        val value = if (iteratorValue.first == null) {
            iteratorValue.second.dereference(analyzer.memory, toWrite = false)
        } else {
            val obj = LxmObject(analyzer.memory)
            obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Key, iteratorValue.first!!)
            obj.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.Value, iteratorValue.second)

            obj
        }

        // Perform the destructuring.
        if (!node.mustBeIdentifier) {
            variable as LxmDestructuring

            when (value) {
                is LxmObject -> variable.destructureObject(analyzer.memory, value, context)
                is LxmList -> variable.destructureList(analyzer.memory, value, context)
                else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                        "Destructuring is only available for ${ObjectType.TypeName}s and ${ListType.TypeName}s.") {
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

            context.setProperty(analyzer.memory, variable.primitive, value)
        }

        return analyzer.nextNode(node.thenBlock)
    }

    /**
     * Process the finalization of the loop.
     */
    private fun finish(analyzer: LexemAnalyzer, node: IteratorLoopStmtCompiled) {
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
