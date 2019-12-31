package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.compiler.descriptive.selectors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*


/**
 * Analyzer for property abbreviations of selectors.
 */
internal object PropertyAbbreviationSelectorAnalyzer {
    const val signalEndName = AnalyzerNodesCommons.signalStart + 1
    const val signalEndPropertyBlock = signalEndName + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: PropertyAbbreviationSelectorCompiled) {
        if (node.isAddition) {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    return analyzer.nextNode(node.name)
                }
                signalEndName -> {
                    if (node.propertyBlock != null) {
                        // Move Last to Key in the stack.
                        analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                        return analyzer.nextNode(node.propertyBlock)
                    }

                    val lxmNode =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node).dereference(analyzer.memory,
                                    toWrite = false) as LxmNode
                    val props = lxmNode.getProperties(toWrite = true)
                    val name = analyzer.memory.getLastFromStack() as LxmString

                    props.setProperty(name.primitive, LxmLogic.from(!node.isNegated))

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()
                }
                signalEndPropertyBlock -> {
                    val lxmNode =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node).dereference(analyzer.memory,
                                    toWrite = false) as LxmNode
                    val props = lxmNode.getProperties(toWrite = true)
                    val name = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key) as LxmString
                    val value = analyzer.memory.getLastFromStack()

                    props.setProperty(name.primitive, value)

                    // Remove Key and Last from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                    analyzer.memory.removeLastFromStack()
                }
            }
        } else {
            when (signal) {
                AnalyzerNodesCommons.signalStart -> {
                    return analyzer.nextNode(node.name)
                }
                signalEndName -> {
                    // Get the property.
                    val lxmNode =
                            analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node).dereference(analyzer.memory,
                                    toWrite = false) as LxmNode
                    val name = analyzer.memory.getLastFromStack() as LxmString

                    val prop: LexemPrimitive? = if (node.isAtIdentifier) {
                        when (name.primitive) {
                            AnalyzerCommons.SelectorAtIdentifiers.Name -> {
                                LxmString.from(lxmNode.name)
                            }
                            AnalyzerCommons.SelectorAtIdentifiers.Content -> {
                                lxmNode.getContent()
                            }
                            AnalyzerCommons.SelectorAtIdentifiers.Start -> {
                                LxmInteger.from(lxmNode.getFrom().primitive.position())
                            }
                            AnalyzerCommons.SelectorAtIdentifiers.End -> {
                                val value = lxmNode.getTo()?.primitive?.position()
                                if (value != null) {
                                    LxmInteger.from(value)
                                } else {
                                    null
                                }
                            }
                            else -> throw AngmarAnalyzerException(
                                    AngmarAnalyzerExceptionType.UnrecognizedSelectorPropertyAtIdentifier,
                                    "Unrecognized '${name.primitive}' at-identifier for a property.") {
                                val fullText = node.parser.reader.readAllText()
                                addSourceCode(fullText, node.parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(node.from.position(), node.to.position() - 1)
                                }
                                addSourceCode(fullText) {
                                    title = Consts.Logger.hintTitle
                                    highlightSection(node.name.from.position(), node.name.to.position() - 1)
                                    message = "Review the at-identifier"
                                }
                            }
                        }
                    } else {
                        val props = lxmNode.getProperties(toWrite = false)
                        props.getPropertyValue(name.primitive)
                    }

                    if (node.isNegated) {
                        analyzer.memory.replaceLastStackCell(LxmLogic.from(prop == null))

                        return analyzer.nextNode(node.parent, node.parentSignal)
                    }

                    if (prop == null) {
                        analyzer.memory.replaceLastStackCell(LxmLogic.False)

                        return analyzer.nextNode(node.parent, node.parentSignal)
                    }

                    // Remove Last from the stack.
                    analyzer.memory.removeLastFromStack()

                    if (node.propertyBlock != null) {
                        // Save the property in the stack.
                        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property, prop)

                        return analyzer.nextNode(node.propertyBlock)
                    }

                    analyzer.memory.addToStackAsLast(LxmLogic.True)
                }
                signalEndPropertyBlock -> {
                    val value = analyzer.memory.getLastFromStack()
                    val valueTruthy = RelationalFunctions.isTruthy(value)

                    analyzer.memory.replaceLastStackCell(LxmLogic.from(valueTruthy))

                    // Remove Property from the stack.
                    analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Property)
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
