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
 * Analyzer for simplification elements of object literals.
 */
internal object ObjectSimplificationAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndParameterList = signalEndIdentifier + 1
    const val signalEndBlock = signalEndParameterList + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: ObjectSimplificationNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            // Create the function and add it to the object.
            signalEndIdentifier -> {
                val identifier = analyzer.memory.getLastFromStack()

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (identifier !is LxmString) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                            "The returned value by the key expression must be a ${StringType.TypeName}.") {
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

                val obj = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Accumulator).dereference(
                        analyzer.memory, toWrite = true) as LxmObject
                val ctxRef = AnalyzerCommons.getCurrentContextReference(analyzer.memory)
                val fn = LxmFunction(analyzer.memory, node, ctxRef)
                val fnRef = analyzer.memory.add(fn)

                obj.setProperty(analyzer.memory, identifier.primitive, fnRef, isConstant = node.isConstant)
            }
            else -> {
                return AnalyzerNodesCommons.functionExecutionController(analyzer, signal, node.parameterList,
                        node.block!!, node, signalEndParameterList, signalEndBlock)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
