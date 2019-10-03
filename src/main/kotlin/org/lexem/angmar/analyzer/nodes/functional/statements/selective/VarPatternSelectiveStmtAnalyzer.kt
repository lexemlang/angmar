package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.selective.*


/**
 * Analyzer for variable patterns of the selective statements.
 */
internal object VarPatternSelectiveStmtAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndConditional = signalEndIdentifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: VarPatternSelectiveStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                val identifier = analyzer.memory.popStack()
                val mainValue = analyzer.memory.popStack()
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)

                // Check identifier if it is not a destructuring.
                if (node.identifier !is DestructuringStmtNode) {
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
                }

                // Perform the destructuring.
                if (node.identifier is DestructuringStmtNode) {
                    identifier as LxmDestructuring

                    when (val derefValue = mainValue.dereference(analyzer.memory)) {
                        is LxmObject -> identifier.destructureObject(analyzer.memory, derefValue, context,
                                node.isConstant)
                        is LxmList -> identifier.destructureList(analyzer.memory, derefValue, context, node.isConstant)
                        else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                                "Destructuring is only available for ${ObjectType.TypeName}s and ${ListType.TypeName}s. Actual value: $derefValue") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                            addNote(Consts.Logger.hintTitle,
                                    "The value is received from the ${SelectiveStmtNode.keyword} statement's condition. Review that value.")
                        }
                    }

                    (mainValue as LxmReference).decreaseReferenceCount(analyzer.memory)
                } else {
                    identifier as LxmString

                    context.setPropertyAsContext(analyzer.memory, identifier.primitive, mainValue,
                            isConstant = node.isConstant)

                    if (mainValue is LxmReference) {
                        mainValue.decreaseReferenceCount(analyzer.memory)
                    }
                }

                if (node.conditional != null) {
                    return analyzer.nextNode(node.conditional)
                }

                analyzer.memory.pushStack(LxmLogic.True)
            }
            signalEndConditional -> {
                // Returns the value of the conditional.
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
