package org.lexem.angmar.analyzer.nodes.descriptive.lexemes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.descriptive.lexemes.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for explicit quantifier lexemes.
 */
internal object ExplicitQuantifierLexemeAnalyzer {
    const val signalEndMinimum = AnalyzerNodesCommons.signalStart + 1
    const val signalEndMaximum = signalEndMinimum + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ExplicitQuantifierLexemeCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.minimum)
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
                        highlightSection(node.minimum.from.position(), node.minimum.to.position() - 1)
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
                            highlightSection(node.minimum.from.position(), node.minimum.to.position() - 1)
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
                val quantifier = LxmQuantifier(minimum.primitive, isInfinite = node.hasMaximum)

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
