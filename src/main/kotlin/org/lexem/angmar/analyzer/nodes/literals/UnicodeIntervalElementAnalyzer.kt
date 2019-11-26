package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.data.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for elements of unicode interval literals.
 */
internal object UnicodeIntervalElementAnalyzer {
    const val signalEndLeft = AnalyzerNodesCommons.signalStart + 1
    const val signalEndRight = signalEndLeft + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: UnicodeIntervalElementNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.left != null) {
                    return analyzer.nextNode(node.left)
                }

                if (node.right != null) {
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmString.from("${node.leftChar}"))
                    return analyzer.nextNode(node.right)
                }

                val right = if (node.rightChar == ' ') {
                    node.leftChar
                } else {
                    node.rightChar
                }

                operate(analyzer, node.leftChar.toString(), right.toString(), node)
            }
            signalEndLeft -> {
                // Check value.
                val left = analyzer.memory.getLastFromStack()

                if (left !is LxmString) {
                    val msg = if (node.right != null) {
                        "The returned value by the left expression must be a ${StringType.TypeName}. Actual value: $left"
                    } else {
                        "The returned value by the expression must be a ${StringType.TypeName}. Actual value: $left"
                    }

                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType, msg) {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.left!!.from.position(), node.left!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                if (left.primitive.isEmpty()) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectRangeBounds,
                            "The returned value by the right expression is an empty ${StringType.TypeName}. To be part of a range it must contain at least one character.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.left!!.from.position(), node.left!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                if (node.right != null) {
                    analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Left)
                    return analyzer.nextNode(node.right)
                }

                val right = if (node.rightChar == ' ') {
                    left.primitive
                } else {
                    node.rightChar.toString()
                }

                operate(analyzer, left.primitive, right, node)

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()
            }
            signalEndRight -> {
                // Check value.
                val right = analyzer.memory.getLastFromStack()

                if (right !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The returned value by the right expression must be a ${StringType.TypeName}. Actual value: $right") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.right!!.from.position(), node.right!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                if (right.primitive.isEmpty()) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectRangeBounds,
                            "The returned value by the right expression is an empty ${StringType.TypeName}. To be part of a range it must contain at least one character.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.right!!.from.position(), node.right!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                // Add the value to the interval.
                val left = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left) as LxmString

                operate(analyzer, left.primitive, right.primitive, node)

                // Remove Left and Last from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Left)
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Creates a Unicode range with the first character of both strings as bounds.
     */
    private fun operate(analyzer: LexemAnalyzer, left: String, right: String, node: UnicodeIntervalElementNode) {
        try {
            val itv = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator) as LxmInterval
            val range = IntegerRange.new(left.codePointAt(0), right.codePointAt(0))
            val resInterval = itv.primitive.plus(range)

            analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Accumulator, LxmInterval.from(resInterval))
        } catch (e: AngmarException) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectRangeBounds,
                    "The left value must be lower or equal than the right value. Actual left: $left, actual right: $right") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(node.from.position(), node.to.position() - 1)
                }
            }
        }
    }
}
