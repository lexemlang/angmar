package org.lexem.angmar.analyzer.nodes.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.functional.statements.selective.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for variable patterns of the selective statements.
 */
internal object VarPatternSelectiveStmtAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndConditional = signalEndIdentifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: VarPatternSelectiveStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                val identifier = analyzer.memory.getLastFromStack()
                val mainValue = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.SelectiveCondition)
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)

                // Check identifier if it is not a destructuring.
                if (node.mustBeIdentifier) {
                    if (identifier !is LxmString) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                                "The returned value by the identifier expression must be a ${StringType.TypeName}.") {
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

                    context.setPropertyAsContext(analyzer.memory, identifier.primitive, mainValue,
                            isConstant = node.isConstant)
                }
                // Perform the destructuring.
                else {
                    identifier as LxmDestructuring
                    
                    when (val derefValue = mainValue.dereference(analyzer.memory, toWrite = false)) {
                        is LxmObject -> identifier.destructureObject(analyzer.memory, derefValue, context,
                                node.isConstant)
                        is LxmList -> identifier.destructureList(analyzer.memory, derefValue, context, node.isConstant)
                        else -> throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                                "Destructuring is only available for ${ObjectType.TypeName}s and ${ListType.TypeName}s.") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                            addNote(Consts.Logger.hintTitle,
                                    "The value is received from the ${SelectiveStmtNode.keyword} statement's condition. Review that value.")
                        }
                    }
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                if (node.conditional != null) {
                    return analyzer.nextNode(node.conditional)
                }

                analyzer.memory.addToStackAsLast(LxmLogic.True)
            }
            signalEndConditional -> {
                // Returns the value of the conditional.
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
