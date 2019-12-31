package org.lexem.angmar.analyzer.nodes.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for variable declaration.
 */
internal object VarDeclarationStmtAnalyzer {
    const val signalEndIdentifier = AnalyzerNodesCommons.signalStart + 1
    const val signalEndValue = signalEndIdentifier + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: VarDeclarationStmtCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.identifier)
            }
            signalEndIdentifier -> {
                // Check identifier if it is not a destructuring.
                if (node.mustBeIdentifier) {
                    val identifier = analyzer.memory.getLastFromStack()

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
                }

                // Move Last to Key in stack.
                analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                return analyzer.nextNode(node.value)
            }
            signalEndValue -> {
                val value = analyzer.memory.getLastFromStack()
                val identifier = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key)
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = true)

                // Perform the destructuring.
                if (!node.mustBeIdentifier) {
                    identifier as LxmDestructuring

                    val varNames = when (val derefValue = value.dereference(analyzer.memory, toWrite = false)) {
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
                            addSourceCode(fullText) {
                                title = Consts.Logger.hintTitle
                                highlightSection(node.value.from.position(), node.value.to.position() - 1)
                                message = "Review the returned value of this expression"
                            }
                        }
                    }

                    // Add it to the exports if the parent is a public macro
                    if (node.parent is PublicMacroStmtCompiled) {
                        val exports = context.getDereferencedProperty<LxmObject>(AnalyzerCommons.Identifiers.Exports,
                                toWrite = true)!!

                        for (varName in varNames) {
                            val descriptor = context.getPropertyDescriptor(varName)!!
                            exports.setProperty(varName, descriptor.value, isConstant = descriptor.isConstant)
                        }
                    }
                } else {
                    identifier as LxmString

                    context.setProperty(identifier.primitive, value, isConstant = node.isConstant)

                    // Add it to the exports if the parent is a public macro.
                    if (node.parent is PublicMacroStmtCompiled) {
                        val exports = context.getDereferencedProperty<LxmObject>(AnalyzerCommons.Identifiers.Exports,
                                toWrite = true)!!

                        exports.setProperty(identifier.primitive, value, isConstant = node.isConstant)
                    }
                }

                // Remove Last and Key from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                analyzer.memory.removeLastFromStack()
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
