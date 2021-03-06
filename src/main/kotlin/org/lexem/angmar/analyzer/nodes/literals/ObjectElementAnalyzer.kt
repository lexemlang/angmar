package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for key-value pairs of object literals.
 */
internal object ObjectElementAnalyzer {
    const val signalEndKey = AnalyzerNodesCommons.signalStart + 1
    const val signalEndValue = signalEndKey + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ObjectElementNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.key)
            }
            signalEndKey -> {
                // Check identifier.
                val identifier = analyzer.memory.getLastFromStack()

                if (identifier !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The returned value by the key expression must be a ${StringType.TypeName}. Actual value: $identifier") {
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

                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                // Add the value to the object.
                val value = analyzer.memory.getLastFromStack()
                val identifier = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key) as LxmString
                val obj = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory) as LxmObject

                obj.setProperty(analyzer.memory, identifier.primitive, value, isConstant = node.isConstant)

                // Remove the key and value from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
