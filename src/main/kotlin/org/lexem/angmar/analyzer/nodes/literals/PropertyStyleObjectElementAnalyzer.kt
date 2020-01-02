package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for property-style object element.
 */
internal object PropertyStyleObjectElementAnalyzer {
    const val signalEndKey = AnalyzerNodesCommons.signalStart + 1
    const val signalEndValue = signalEndKey + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PropertyStyleObjectElementCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.key)
            }
            signalEndKey -> {
                val key = analyzer.memory.getLastFromStack()

                if (key !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The returned value by the key expression must be a ${StringType.TypeName}.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.key.from.position(), node.key.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                // Move last to key.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                val value = analyzer.memory.getLastFromStack()
                val key = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key) as LxmString
                val obj = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory, toWrite = true) as LxmObject

                obj.setProperty(analyzer.memory, key.primitive, value)

                // Remove the key and value from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
