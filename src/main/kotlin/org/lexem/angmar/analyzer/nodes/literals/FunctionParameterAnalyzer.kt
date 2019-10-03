package org.lexem.angmar.analyzer.nodes.literals

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.literals.*


/**
 * Analyzer for function parameters.
 */
internal object FunctionParameterAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndExpression = signalEndIdentifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: FunctionParameterNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                // Check identifier.
                val identifier = analyzer.memory.popStack()

                if (identifier !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The returned value by the identifier expression must be a ${StringType.TypeName}. Actual value: $identifier") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.identifier.from.position(), node.identifier.to.position() - 1)
                            message = "Review the returned value of this expression"
                        }
                    }
                }

                // Add to the parameters.
                val parameters = analyzer.memory.popStack() as LxmParameters
                parameters.addParameter(identifier.primitive)

                analyzer.memory.pushStack(parameters)

                if (node.expression != null) {
                    analyzer.memory.pushStack(identifier)
                    return analyzer.nextNode(node.expression)
                }

                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, identifier.primitive, LxmNil)
            }
            signalEndExpression -> {
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val value = analyzer.memory.popStack()
                val identifier = analyzer.memory.popStack() as LxmString

                context.setProperty(analyzer.memory, identifier.primitive, value)

                // Decrement the reference count of the value.
                if (value is LxmReference) {
                    value.decreaseReferenceCount(analyzer.memory)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
