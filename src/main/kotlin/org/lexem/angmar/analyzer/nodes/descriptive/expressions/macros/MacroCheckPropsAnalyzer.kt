package org.lexem.angmar.analyzer.nodes.descriptive.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.parser.descriptive.expressions.macros.*


/**
 * Analyzer for macro 'check props'.
 */
internal object MacroCheckPropsAnalyzer {
    const val signalEndValue = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MacroCheckPropsNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                val valuesRef = analyzer.memory.getLastFromStack()
                val values = valuesRef.dereference(analyzer.memory) as LxmObject
                val properties = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)

                for ((key, value) in values.getAllIterableProperties()) {
                    val property = properties.getPropertyValue(analyzer.memory, key)

                    if (!RelationalFunctions.identityEquals(value.value, property ?: LxmNil)) {
                        // Return false.
                        analyzer.memory.replaceLastStackCell(LxmLogic.False)

                        return analyzer.nextNode(node.parent, node.parentSignal)
                    }
                }

                // Return true.
                analyzer.memory.replaceLastStackCell(LxmLogic.True)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
