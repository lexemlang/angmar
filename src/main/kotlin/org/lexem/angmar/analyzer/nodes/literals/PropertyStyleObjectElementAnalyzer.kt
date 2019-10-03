package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for property-style object element.
 */
internal object PropertyStyleObjectElementAnalyzer {
    const val signalEndKey = AnalyzerNodesCommons.signalStart + 1
    const val signalEndValue = signalEndKey + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PropertyStyleObjectElementNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.key)
            }
            signalEndKey -> {
                val key = analyzer.memory.popStack()

                if (key !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The returned value by the key expression must be a ${StringType.TypeName}. Actual value: $key") {
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

                analyzer.memory.pushStack(key)
                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                val value = analyzer.memory.popStack()
                val key = analyzer.memory.popStack() as LxmString
                val objRef = analyzer.memory.popStack()
                val obj = objRef.dereference(analyzer.memory) as LxmObject

                obj.setProperty(analyzer.memory, key.primitive, value)

                analyzer.memory.pushStackIgnoringReferenceCount(objRef)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
