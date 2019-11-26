package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.TextLexemAnalyzer.signalEndText
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.*


/**
 * Analyzer for quantified group modifiers.
 */
internal object QuantifiedGroupModifierAnalyzer {
    const val signalEndMinimum = AnalyzerNodesCommons.signalStart + 1
    const val signalEndMaximum = signalEndText + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: QuantifiedGroupModifierNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                if (node.minimum != null) {
                    return analyzer.nextNode(node.minimum)
                }

                if (node.maximum != null) {
                    // Add minimum.
                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Left, LxmInteger.Num_1)

                    return analyzer.nextNode(node.maximum)
                }

                val quantifier = LxmQuantifier(-1)

                analyzer.memory.addToStackAsLast(quantifier)
            }
            signalEndMinimum -> {
                // Get minimum value
                val minimum = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.IncompatibleType,
                        "The minimum value of a quantifier must be of type ${IntegerType.TypeName}.") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                    addSourceCode(fullText) {
                        title = Consts.Logger.hintTitle
                        highlightSection(node.minimum!!.from.position(), node.minimum!!.to.position() - 1)
                        message = "Review the returned value of this expression"
                    }
                }

                if (minimum.primitive < 0) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectQuantifierBounds,
                            "The minimum value of a quantifier cannot be negative. Actual: $minimum") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.minimum!!.from.position(), node.minimum!!.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                if (node.maximum != null) {
                    // Move Last to Left in the stack.
                    analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Left)

                    return analyzer.nextNode(node.maximum)
                }

                // Create quantifier
                val quantifier = if (node.hasMaximum) {
                    LxmQuantifier(minimum.primitive, -1)
                } else {
                    LxmQuantifier(minimum.primitive)
                }

                analyzer.memory.replaceLastStackCell(quantifier)
            }
            signalEndMaximum -> {
                // Get minimum and maximum values
                val maximum = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.IncompatibleType,
                        "The maximum value of a quantifier must be of type ${IntegerType.TypeName}.") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                    addSourceCode(fullText) {
                        title = Consts.Logger.hintTitle
                        highlightSection(node.maximum!!.from.position(), node.maximum!!.to.position() - 1)
                        message = "Review the returned value of this expression"
                    }
                }

                val minimum = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Left) as LxmInteger

                if (maximum.primitive < minimum.primitive) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectQuantifierBounds,
                            "The maximum value cannot be lower than the minimum. Actual: {min: $minimum, max: $maximum}") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addNote(Consts.Logger.hintTitle, "Review the returned values of both expressions")
                    }
                }

                // Create quantifier
                val quantifier = LxmQuantifier(minimum.primitive, maximum.primitive)

                // Remove Left from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Left)

                analyzer.memory.replaceLastStackCell(quantifier)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
