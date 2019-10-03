package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Analyzer for variable declaration.
 */
internal object VarDeclarationStmtAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndValue = signalEndIdentifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: VarDeclarationStmtNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                // Check identifier if it is not a destructuring.
                if (node.identifier !is DestructuringStmtNode) {
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

                    analyzer.memory.pushStack(identifier)
                }

                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                val value = analyzer.memory.popStack()
                val identifier = analyzer.memory.popStack()
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)

                // Perform the destructuring.
                if (node.identifier is DestructuringStmtNode) {
                    identifier as LxmDestructuring

                    val varNames = when (val derefValue = value.dereference(analyzer.memory)) {
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
                            addSourceCode(fullText) {
                                title = Consts.Logger.hintTitle
                                highlightSection(node.value.from.position(), node.value.to.position() - 1)
                                message = "Review the returned value of this expression"
                            }
                        }
                    }

                    // Add it to the exports if the parent is a public macro
                    if (node.parent is PublicMacroStmtNode) {
                        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory,
                                AnalyzerCommons.Identifiers.Exports)!!

                        for (varName in varNames) {
                            val descriptor = context.getPropertyDescriptor(analyzer.memory, varName)!!
                            exports.setProperty(analyzer.memory, varName, descriptor.value,
                                    isConstant = descriptor.isConstant)
                        }
                    }

                    // Reduce the reference of the value.
                    (value as LxmReference).decreaseReferenceCount(analyzer.memory)
                } else {
                    identifier as LxmString

                    context.setPropertyAsContext(analyzer.memory, identifier.primitive, value,
                            isConstant = node.isConstant)

                    // Add it to the exports if the parent is a public macro
                    if (node.parent is PublicMacroStmtNode) {
                        val exports = context.getDereferencedProperty<LxmObject>(analyzer.memory,
                                AnalyzerCommons.Identifiers.Exports)!!

                        exports.setProperty(analyzer.memory, identifier.primitive, value, isConstant = node.isConstant)
                    }

                    if (value is LxmReference) {
                        value.decreaseReferenceCount(analyzer.memory)
                    }
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
