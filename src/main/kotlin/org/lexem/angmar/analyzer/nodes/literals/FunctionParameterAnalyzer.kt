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
                val identifier = analyzer.memory.getLastFromStack()

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
                val parameters = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Parameters) as LxmParameters
                parameters.addParameter(identifier.primitive)

                if (node.expression != null) {
                    // Move Last to Key in stack.
                    analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                    return analyzer.nextNode(node.expression)
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                context.setProperty(analyzer.memory, identifier.primitive, LxmNil)
            }
            signalEndExpression -> {
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val value = analyzer.memory.getLastFromStack()
                val identifier = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key) as LxmString

                context.setProperty(analyzer.memory, identifier.primitive, value)

                // Remove Last and Key from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
