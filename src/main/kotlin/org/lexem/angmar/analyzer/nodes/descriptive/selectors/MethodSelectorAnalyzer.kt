package org.lexem.angmar.analyzer.nodes.descriptive.selectors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.data.referenced.iterators.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.selectors.*


/**
 * Analyzer for methods of selectors.
 */
internal object MethodSelectorAnalyzer {
    const val signalEndName = AnalyzerNodesCommons.signalStart + 1
    const val signalEndArgument = signalEndName + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MethodSelectorNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.name)
            }
            signalEndName -> {
                val name = analyzer.memory.getLastFromStack() as LxmString

                if (name.primitive !in AnalyzerCommons.SelectorMethods.all) {
                    throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UnrecognizedSelectorMethod,
                            "Unrecognized '${name.primitive}' selector method.") {
                        val fullText = node.parser.reader.readAllText()
                        addSourceCode(fullText, node.parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(node.from.position(), node.to.position() - 1)
                        }
                        addSourceCode(fullText) {
                            title = Consts.Logger.hintTitle
                            highlightSection(node.name.from.position(), node.name.to.position() - 1)
                            message = "Review the method name is correct"
                        }
                    }
                }

                when (node.argument) {
                    null -> {
                        if (name.primitive !in AnalyzerCommons.SelectorMethods.allSimple) {
                            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments,
                                    "Cannot use the '${name.primitive}' selector method without an argument.") {
                                val fullText = node.parser.reader.readAllText()
                                addSourceCode(fullText, node.parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(node.from.position(), node.to.position() - 1)
                                }

                                if (name.primitive !in AnalyzerCommons.SelectorMethods.AllWithCondition) {
                                    addSourceCode(fullText) {
                                        title = Consts.Logger.hintTitle
                                        highlightCursorAt(node.to.position())
                                        message =
                                                "Add a condition here, i.e. ${PropertyBlockSelectorNode.startToken}...${PropertyBlockSelectorNode.endToken}"
                                    }
                                } else {
                                    addSourceCode(fullText) {
                                        title = Consts.Logger.hintTitle
                                        highlightCursorAt(node.to.position())
                                        message =
                                                "Add a selector here, i.e. ${MethodSelectorNode.selectorStartToken}...${MethodSelectorNode.selectorEndToken}"
                                    }
                                }
                            }
                        }

                        val lxmNodeRef = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node)
                        val lxmNode = lxmNodeRef.dereference(analyzer.memory) as LxmNode

                        when (name.primitive) {
                            AnalyzerCommons.SelectorMethods.Root -> {
                                val parent = lxmNode.getParent(analyzer.memory)

                                analyzer.memory.replaceLastStackCell(generateResult(node, parent == null))
                            }
                            AnalyzerCommons.SelectorMethods.Empty -> {
                                val children = lxmNode.getChildren(analyzer.memory)

                                analyzer.memory.replaceLastStackCell(generateResult(node, children.listSize == 0))
                            }
                            AnalyzerCommons.SelectorMethods.FirstChild -> {
                                val parent = lxmNode.getParent(analyzer.memory)

                                if (parent == null) {
                                    analyzer.memory.replaceLastStackCell(generateResult(node, false))
                                } else {
                                    val list = parent.getChildrenAsList(analyzer.memory)
                                    val position =
                                            list.indexOfFirst { RelationalFunctions.identityEquals(it, lxmNodeRef) }

                                    analyzer.memory.replaceLastStackCell(generateResult(node, position == 0))
                                }
                            }
                            AnalyzerCommons.SelectorMethods.LastChild -> {
                                val parent = lxmNode.getParent(analyzer.memory)

                                if (parent == null) {
                                    analyzer.memory.replaceLastStackCell(generateResult(node, false))
                                } else {
                                    val list = parent.getChildrenAsList(analyzer.memory)
                                    val position =
                                            list.indexOfFirst { RelationalFunctions.identityEquals(it, lxmNodeRef) }

                                    analyzer.memory.replaceLastStackCell(
                                            generateResult(node, position == list.size - 1))
                                }
                            }
                            else -> throw AngmarUnreachableException()
                        }
                    }
                    is SelectorNode -> {
                        if (name.primitive !in AnalyzerCommons.SelectorMethods.allWithSelectors) {
                            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments,
                                    "Cannot use the '${name.primitive}' selector method with a selector argument.") {
                                val fullText = node.parser.reader.readAllText()
                                addSourceCode(fullText, node.parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(node.from.position(), node.to.position() - 1)
                                }

                                if (name.primitive in AnalyzerCommons.SelectorMethods.allSimple) {
                                    addSourceCode(fullText) {
                                        title = Consts.Logger.hintTitle
                                        highlightSection(
                                                node.argument!!.from.position() - MethodSelectorNode.selectorStartToken.length,
                                                node.argument!!.to.position() + MethodSelectorNode.selectorEndToken.length - 1)
                                        message = "Remove the argument"
                                    }
                                } else {
                                    addSourceCode(fullText) {
                                        title = Consts.Logger.hintTitle
                                        highlightSection(
                                                node.argument!!.from.position() - MethodSelectorNode.selectorStartToken.length,
                                                node.argument!!.to.position() + MethodSelectorNode.selectorEndToken.length - 1)
                                        message =
                                                "Replace the argument by a condition, i.e. ${PropertyBlockSelectorNode.startToken}...${PropertyBlockSelectorNode.endToken}"
                                    }
                                }
                            }
                        }

                        // Move Last to Key in the stack.
                        analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                        val lxmNodeRef = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node)
                        val lxmNode = lxmNodeRef.dereference(analyzer.memory) as LxmNode

                        when (name.primitive) {
                            AnalyzerCommons.SelectorMethods.Parent -> {
                                val parent = lxmNode.getParentReference(analyzer.memory)

                                if (parent != null) {
                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, parent)

                                    return analyzer.nextNode(node.argument)
                                }

                                // Remove Key from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)

                                analyzer.memory.addToStackAsLast(generateResult(node, false))
                            }
                            AnalyzerCommons.SelectorMethods.Node -> {
                                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node, lxmNodeRef)

                                return analyzer.nextNode(node.argument)
                            }
                            AnalyzerCommons.SelectorMethods.AllChildren -> {
                                val childrenRef = lxmNode.getChildrenReference(analyzer.memory)
                                val children = childrenRef.dereferenceAs<LxmList>(analyzer.memory)!!

                                if (children.listSize > 0) {
                                    val iterator = LxmListIterator(analyzer.memory, childrenRef)
                                    val iteratorRef = analyzer.memory.add(iterator)

                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node,
                                            children.getCell(analyzer.memory, 0)!!)
                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectorMethodIterator,
                                            iteratorRef)

                                    return analyzer.nextNode(node.argument)
                                }

                                // Remove Key from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)

                                analyzer.memory.addToStackAsLast(generateResult(node, true))
                            }
                            AnalyzerCommons.SelectorMethods.AnyChild -> {
                                val childrenRef = lxmNode.getChildrenReference(analyzer.memory)
                                val children = childrenRef.dereferenceAs<LxmList>(analyzer.memory)!!

                                if (children.listSize > 0) {
                                    val iterator = LxmListIterator(analyzer.memory, childrenRef)
                                    val iteratorRef = analyzer.memory.add(iterator)

                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Node,
                                            children.getCell(analyzer.memory, 0)!!)
                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectorMethodIterator,
                                            iteratorRef)

                                    return analyzer.nextNode(node.argument)
                                }

                                // Remove Key from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)

                                analyzer.memory.addToStackAsLast(generateResult(node, false))
                            }
                            else -> throw AngmarUnreachableException()
                        }
                    }
                    is PropertyBlockSelectorNode -> {
                        if (name.primitive !in AnalyzerCommons.SelectorMethods.AllWithCondition) {
                            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncorrectSelectorMethodArguments,
                                    "Cannot use the '${name.primitive}' selector method with a conditional argument.") {
                                val fullText = node.parser.reader.readAllText()
                                addSourceCode(fullText, node.parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(node.from.position(), node.to.position() - 1)
                                }

                                if (name.primitive in AnalyzerCommons.SelectorMethods.allSimple) {
                                    addSourceCode(fullText) {
                                        title = Consts.Logger.hintTitle
                                        highlightSection(node.argument!!.from.position(),
                                                node.argument!!.to.position() - 1)
                                        message = "Remove the argument"
                                    }
                                } else {
                                    addSourceCode(fullText) {
                                        title = Consts.Logger.hintTitle
                                        highlightSection(node.argument!!.from.position(),
                                                node.argument!!.to.position() - 1)
                                        message =
                                                "Replace the argument by a selector, i.e. ${MethodSelectorNode.selectorStartToken}...${MethodSelectorNode.selectorEndToken}"
                                    }
                                }
                            }
                        }

                        // Move Last to Key in the stack.
                        analyzer.memory.renameLastStackCell(AnalyzerCommons.Identifiers.Key)

                        val lxmNodeRef = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Node)
                        val lxmNode = lxmNodeRef.dereference(analyzer.memory) as LxmNode

                        when (name.primitive) {
                            AnalyzerCommons.SelectorMethods.ChildCount -> {
                                val children = lxmNode.getChildren(analyzer.memory)

                                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property,
                                        LxmInteger.from(children.listSize))

                                return analyzer.nextNode(node.argument)
                            }
                            AnalyzerCommons.SelectorMethods.NthChild -> {
                                val parent = lxmNode.getParent(analyzer.memory)

                                if (parent == null) {
                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property, LxmNil)
                                } else {
                                    val list = parent.getChildrenAsList(analyzer.memory)
                                    val position =
                                            list.indexOfFirst { RelationalFunctions.identityEquals(it, lxmNodeRef) }

                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property,
                                            LxmInteger.from(position))
                                }

                                return analyzer.nextNode(node.argument)
                            }
                            AnalyzerCommons.SelectorMethods.Node -> {
                                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property, lxmNodeRef)

                                return analyzer.nextNode(node.argument)
                            }
                            AnalyzerCommons.SelectorMethods.Parent -> {
                                val parent = lxmNode.getParentReference(analyzer.memory)

                                if (parent != null) {
                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property, parent)

                                    return analyzer.nextNode(node.argument)
                                }

                                // Remove Key from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)

                                analyzer.memory.addToStackAsLast(generateResult(node, false))
                            }
                            AnalyzerCommons.SelectorMethods.AllChildren -> {
                                val childrenRef = lxmNode.getChildrenReference(analyzer.memory)
                                val children = childrenRef.dereferenceAs<LxmList>(analyzer.memory)!!

                                if (children.listSize > 0) {
                                    val iterator = LxmListIterator(analyzer.memory, childrenRef)
                                    val iteratorRef = analyzer.memory.add(iterator)

                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property,
                                            children.getCell(analyzer.memory, 0)!!)
                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectorMethodIterator,
                                            iteratorRef)

                                    return analyzer.nextNode(node.argument)
                                }

                                // Remove Key from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)

                                analyzer.memory.addToStackAsLast(generateResult(node, true))
                            }
                            AnalyzerCommons.SelectorMethods.AnyChild -> {
                                val childrenRef = lxmNode.getChildrenReference(analyzer.memory)
                                val children = childrenRef.dereferenceAs<LxmList>(analyzer.memory)!!

                                if (children.listSize > 0) {
                                    val iterator = LxmListIterator(analyzer.memory, childrenRef)
                                    val iteratorRef = analyzer.memory.add(iterator)

                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Property,
                                            children.getCell(analyzer.memory, 0)!!)
                                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.SelectorMethodIterator,
                                            iteratorRef)

                                    return analyzer.nextNode(node.argument)
                                }

                                // Remove Key from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)

                                analyzer.memory.addToStackAsLast(generateResult(node, false))
                            }
                            else -> throw AngmarUnreachableException()
                        }
                    }
                    else -> throw AngmarUnreachableException()
                }
            }
            signalEndArgument -> {
                val name = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Key) as LxmString
                val result = analyzer.memory.getLastFromStack() as LxmLogic

                when (node.argument) {
                    is SelectorNode -> {
                        when (name.primitive) {
                            AnalyzerCommons.SelectorMethods.Parent -> {
                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))
                            }
                            AnalyzerCommons.SelectorMethods.Node -> {
                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))
                            }
                            AnalyzerCommons.SelectorMethods.AllChildren -> {
                                if (result.primitive) {
                                    val iterator = analyzer.memory.getFromStack(
                                            AnalyzerCommons.Identifiers.SelectorMethodIterator).dereference(
                                            analyzer.memory) as LexemIterator

                                    iterator.advance(analyzer.memory)

                                    if (!iterator.isEnded(analyzer.memory)) {
                                        // Remove Last from the stack.
                                        analyzer.memory.removeLastFromStack()

                                        // Replace Node in the stack.
                                        val current = iterator.getCurrent(analyzer.memory)!!
                                        analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Node,
                                                current.second)

                                        return analyzer.nextNode(node.argument)
                                    }
                                }

                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))

                                // Remove SelectorMethodIterator from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectorMethodIterator)
                            }
                            AnalyzerCommons.SelectorMethods.AnyChild -> {
                                if (!result.primitive) {
                                    val iterator = analyzer.memory.getFromStack(
                                            AnalyzerCommons.Identifiers.SelectorMethodIterator).dereference(
                                            analyzer.memory) as LexemIterator

                                    iterator.advance(analyzer.memory)

                                    if (!iterator.isEnded(analyzer.memory)) {
                                        // Remove Last from the stack.
                                        analyzer.memory.removeLastFromStack()

                                        // Replace Node in the stack.
                                        val current = iterator.getCurrent(analyzer.memory)!!
                                        analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Node,
                                                current.second)

                                        return analyzer.nextNode(node.argument)
                                    }
                                }

                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))

                                // Remove SelectorMethodIterator from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectorMethodIterator)
                            }
                            else -> throw AngmarUnreachableException()
                        }

                        // Remove Key and Node from the stack.
                        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Node)
                    }
                    is PropertyBlockSelectorNode -> {
                        when (name.primitive) {
                            AnalyzerCommons.SelectorMethods.Parent -> {
                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))
                            }
                            AnalyzerCommons.SelectorMethods.Node -> {
                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))
                            }
                            AnalyzerCommons.SelectorMethods.ChildCount -> {
                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))
                            }
                            AnalyzerCommons.SelectorMethods.NthChild -> {
                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))
                            }
                            AnalyzerCommons.SelectorMethods.AllChildren -> {
                                if (result.primitive) {
                                    val iterator = analyzer.memory.getFromStack(
                                            AnalyzerCommons.Identifiers.SelectorMethodIterator).dereference(
                                            analyzer.memory) as LexemIterator

                                    iterator.advance(analyzer.memory)

                                    if (!iterator.isEnded(analyzer.memory)) {
                                        // Remove Last from the stack.
                                        analyzer.memory.removeLastFromStack()

                                        // Replace Node in the stack.
                                        val current = iterator.getCurrent(analyzer.memory)!!
                                        analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Property,
                                                current.second)

                                        return analyzer.nextNode(node.argument)
                                    }
                                }

                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))

                                // Remove SelectorMethodIterator from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectorMethodIterator)
                            }
                            AnalyzerCommons.SelectorMethods.AnyChild -> {
                                if (!result.primitive) {
                                    val iterator = analyzer.memory.getFromStack(
                                            AnalyzerCommons.Identifiers.SelectorMethodIterator).dereference(
                                            analyzer.memory) as LexemIterator

                                    iterator.advance(analyzer.memory)

                                    if (!iterator.isEnded(analyzer.memory)) {
                                        // Remove Last from the stack.
                                        analyzer.memory.removeLastFromStack()

                                        // Replace Node in the stack.
                                        val current = iterator.getCurrent(analyzer.memory)!!
                                        analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.Property,
                                                current.second)

                                        return analyzer.nextNode(node.argument)
                                    }
                                }

                                analyzer.memory.replaceLastStackCell(generateResult(node, result.primitive))

                                // Remove SelectorMethodIterator from the stack.
                                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.SelectorMethodIterator)
                            }
                            else -> throw AngmarUnreachableException()
                        }

                        // Remove Key and Property from the stack.
                        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Key)
                        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Property)
                    }
                    else -> throw AngmarUnreachableException()
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Generates the result depending on the node.
     */
    private fun generateResult(node: MethodSelectorNode, result: Boolean) = if (node.isNegated) {
        LxmLogic.from(!result)
    } else {
        LxmLogic.from(result)
    }
}
