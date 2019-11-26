package org.lexem.angmar.analyzer.nodes.descriptive

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.parser.descriptive.*


/**
 * Analyzer for lexeme pattern contents.
 */
internal object LexemePatternContentAnalyzer {
    const val signalEndFirstLexeme = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: LexemePatternContentNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.lexemes.first())
            }
            in signalEndFirstLexeme until signalEndFirstLexeme + node.lexemes.size -> {
                val position = (signal - signalEndFirstLexeme) + 1

                // Evaluate the next lexeme.
                if (position < node.lexemes.size) {
                    return analyzer.nextNode(node.lexemes[position])
                }
            }
            // Propagate the control signal.
            AnalyzerNodesCommons.signalExitControl, AnalyzerNodesCommons.signalNextControl, AnalyzerNodesCommons.signalRedoControl -> {
                return analyzer.nextNode(node.parent, signal)
            }
            AnalyzerNodesCommons.signalRestartControl, AnalyzerNodesCommons.signalReturnControl -> {
                return analyzer.nextNode(node.parent, signal)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
