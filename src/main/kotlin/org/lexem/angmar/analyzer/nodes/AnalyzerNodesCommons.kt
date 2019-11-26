package org.lexem.angmar.analyzer.nodes

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.lexemes.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.literals.*

/**
 * Generic commons for the analyzers.
 */
internal object AnalyzerNodesCommons {
    const val signalStart = 0
    const val signalEndFirstCall = signalStart + 1
    const val signalCallFunction = signalStart - 1
    const val signalExitControl = signalCallFunction - 1
    const val signalNextControl = signalExitControl - 1
    const val signalRedoControl = signalNextControl - 1
    const val signalRestartControl = signalRedoControl - 1
    const val signalReturnControl = signalRestartControl - 1

    // METHODS ----------------------------------------------------------------

    /**
     * Calls a function value.
     * Requires the arguments to have at least one reference count.
     */
    fun callFunction(analyzer: LexemAnalyzer, primitive: LexemPrimitive, arguments: LxmReference, node: ParserNode,
            returnPoint: LxmCodePoint) {
        val function = primitive.dereference(analyzer.memory)
        if (function !is ExecutableValue) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                    "The value is not callable, i.e. a function, expression or filter. Current: $primitive") {
                val fullText = node.parser.reader.readAllText()
                addSourceCode(fullText, node.parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(node.from.position(), node.to.position() - 1)
                    message = "Cannot perform the invocation of a non-callable element"
                }
            }
        }

        // Save the return position.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.ReturnCodePoint, returnPoint)

        // Push the arguments.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Arguments, arguments)

        // Push the reference to the function.
        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.Function, primitive)

        // Generate an intermediate context that will be removed at the end.
        AnalyzerCommons.createAndAssignNewFunctionContext(analyzer.memory,
                function.parentContext ?: LxmReference.StdLibContext)

        // Call the function
        analyzer.nextNode(function.parserNode, signalCallFunction)
    }

    /**
     * Gets the primitive value resolving the setter.
     */
    fun resolveSetter(memory: LexemMemory, value: LexemPrimitive) = if (value is LexemSetter) {
        value.getPrimitive(memory)
    } else {
        value
    }

    /**
     * Handles the function execution.
     */
    fun functionExecutionController(analyzer: LexemAnalyzer, signal: Int, parameterList: FunctionParameterListNode?,
            block: ParserNode, node: ParserNode, signalEndParameterList: Int, signalEndBlock: Int) {
        when (signal) {
            // Call the function.
            signalCallFunction -> {
                if (parameterList != null) {
                    return analyzer.nextNode(parameterList)
                }

                // Set the arguments.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val arguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(
                        analyzer.memory) as LxmArguments
                arguments.mapArgumentsToContext(analyzer.memory, LxmParameters(), context)

                // Remove Arguments from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

                return analyzer.nextNode(block)
            }
            // Parse the arguments.
            signalEndParameterList -> {
                return analyzer.nextNode(block)
            }
            // Finalize the calling.
            signalEndBlock -> {
                // Get the last point to return to it.
                val lastCodePoint =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint) as LxmCodePoint

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

                // Set the returned value.
                analyzer.memory.addToStackAsLast(LxmNil)

                // Remove Control, Function and ReturnCodePoint from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Function)
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint)

                return analyzer.nextNode(lastCodePoint)
            }
            // Throw an error.
            signalExitControl, signalNextControl, signalRedoControl, signalRestartControl -> {
                // Get the control.
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl

                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal,
                        "The ${control.type} control signal has not reached any valid statement.") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.hintTitle
                        highlightSection(control.node.from.position(), control.node.to.position() - 1)
                        message = "Review that this control statement has a matching statement."
                    }

                    if (control.tag != null) {
                        addNote(Consts.Logger.hintTitle, "Check that any statement has the tag: ${control.tag}")

                        val name = AnalyzerCommons.getCurrentContextTag(analyzer.memory)
                        if (name != null && name == control.tag) {
                            addNote(Consts.Logger.hintTitle,
                                    "A tag in the block of a function cannot receive the ${control.type} control signal")
                        }
                    }
                }
            }
            signalReturnControl -> {
                // Get the control
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl

                // Get the last point to return to it.
                val lastCodePoint =
                        analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint) as LxmCodePoint

                // Remove the intermediate context.
                AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

                // Set the returned value.
                analyzer.memory.addToStackAsLast(control.value!!)

                // Remove Control, Function and ReturnCodePoint from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Function)
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint)

                return analyzer.nextNode(lastCodePoint)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    /**
     * Handles the expression or filter execution.
     */
    fun descriptiveExecutionController(analyzer: LexemAnalyzer, signal: Int,
            propertyNode: PropertyStyleObjectBlockNode?, parameterList: FunctionParameterListNode?, block: ParserNode,
            node: ParserNode, signalEndProperties: Int, signalEndParameterList: Int, signalEndBlock: Int) {
        when (signal) {
            // Call the function.
            signalCallFunction -> {
                // Checks whether the current call is a re-parsing.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val node2ReParse =
                        context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.HiddenNode2Filter)
                val arguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(
                        analyzer.memory) as LxmArguments
                if (node2ReParse != null) {
                    if (node is ExpressionStmtNode) {
                        // Save the current text.
                        context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenAnalyzerText,
                                LxmReader(analyzer.text))

                        // Prepare the re-parsing.
                        val node2ReParseDeref = node2ReParse.dereference(analyzer.memory) as LxmNode
                        val content = node2ReParseDeref.getContent(analyzer.memory)!!
                        val reader = AnalyzerCommons.createReaderFrom(content)
                        analyzer.text = reader
                    } else {
                        analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, node2ReParse)
                    }
                }

                // Checks whether the current call is a filtering.
                if (node is FilterStmtNode) {
                    val node2Filter = arguments.getNamedArgument(analyzer.memory,
                            AnalyzerCommons.Identifiers.Node2FilterParameter)

                    if (node2Filter == null) {
                        if (node2ReParse == null) {
                            throw AngmarAnalyzerException(
                                    AngmarAnalyzerExceptionType.FilterCallWithoutNode2FilterArgument,
                                    "Cannot call a filter without specifying the '${AnalyzerCommons.Identifiers.Node2FilterParameter}' argument.") {}
                        }
                    } else {
                        if (node2ReParse == null) {
                            analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNode, node2Filter)
                        } else {
                            analyzer.memory.replaceStackCell(AnalyzerCommons.Identifiers.FilterNode, node2Filter)
                        }
                    }

                    analyzer.memory.addToStack(AnalyzerCommons.Identifiers.FilterNodePosition, LxmInteger.Num0)
                }

                // Save the first index.
                val memoryIndex = LxmBigNode(analyzer.memory.lastNode)
                analyzer.memory.addToStack(AnalyzerCommons.Identifiers.LastNode, memoryIndex)

                // Create node.
                val expression = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Function).dereference(
                        analyzer.memory) as LxmFunction
                val name = if (node is FilterStmtNode) {
                    val node = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.FilterNode).dereference(
                            analyzer.memory) as LxmNode
                    node.name
                } else {
                    expression.name
                }
                val lxmNodeRef = analyzer.createNewNode(name)
                val lxmNode = lxmNodeRef.dereferenceAs<LxmNode>(analyzer.memory)!!
                lxmNode.applyDefaultPropertiesForExpression(analyzer.memory)
                context.setPropertyAsContext(analyzer.memory, AnalyzerCommons.Identifiers.Node, lxmNodeRef,
                        isConstant = true)

                // Create the union container.
                val unions = LxmObject()
                context.setProperty(analyzer.memory, AnalyzerCommons.Identifiers.HiddenPatternUnions,
                        analyzer.memory.add(unions))

                if (propertyNode != null) {
                    return analyzer.nextNode(propertyNode)
                }

                if (parameterList != null) {
                    return analyzer.nextNode(parameterList)
                }

                // Set the arguments.
                arguments.mapArgumentsToContext(analyzer.memory, LxmParameters(), context)

                // Remove Arguments from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

                return analyzer.nextNode(block)
            }
            // Parse the propertyNode.
            signalEndProperties -> {
                // Set the propertyNode.
                val nodeProperties = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)
                val propertiesRef = analyzer.memory.getLastFromStack() as LxmReference
                val properties = propertiesRef.dereferenceAs<LxmObject>(analyzer.memory)!!

                for ((name, property) in properties.getAllIterableProperties()) {
                    nodeProperties.setProperty(analyzer.memory, name, property.value)
                }

                // Remove Last from the stack.
                analyzer.memory.removeLastFromStack()

                // Execute the parameter list.
                if (parameterList != null) {
                    return analyzer.nextNode(parameterList)
                }

                // Set the arguments.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val arguments = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Arguments).dereference(
                        analyzer.memory) as LxmArguments
                arguments.mapArgumentsToContext(analyzer.memory, LxmParameters(), context)

                // Remove Arguments from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Arguments)

                return analyzer.nextNode(block)
            }
            // Parse the arguments.
            signalEndParameterList -> {
                // Move the properties parameter to the node.
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory)
                val properties =
                        context.getOwnPropertyDescriptor(analyzer.memory, AnalyzerCommons.Identifiers.Properties)
                                ?.value?.dereference(analyzer.memory)

                if (properties != null) {
                    if (properties !is LxmObject) {
                        throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                                "The value of the '${AnalyzerCommons.Identifiers.Properties}' parameter inside an expression must always be of type ${ObjectType.TypeName}") {
                            val fullText = node.parser.reader.readAllText()
                            addSourceCode(fullText, node.parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(node.from.position(), node.to.position() - 1)
                            }
                        }
                    }

                    val nodeProperties = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)
                    for ((name, property) in properties.getAllIterableProperties()) {
                        nodeProperties.setProperty(analyzer.memory, name, property.value)
                    }
                }

                return analyzer.nextNode(block)
            }
            // Finalize the calling.
            signalEndBlock -> {
                return descriptiveExecutionControllerFinal(analyzer, node)
            }
            // Throw an error.
            signalExitControl, signalNextControl, signalRedoControl, signalRestartControl -> {
                // Get the control
                val control = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.Control) as LxmControl

                throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.UnhandledControlStatementSignal,
                        "The ${control.type} control signal has not reached any valid statement.") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.hintTitle
                        highlightSection(control.node.from.position(), control.node.to.position() - 1)
                        message = "Review that this control statement has a matching statement."
                    }

                    if (control.tag != null) {
                        addNote(Consts.Logger.hintTitle, "Check that any statement has the tag: ${control.tag}")

                        val name = AnalyzerCommons.getCurrentContextTag(analyzer.memory)
                        if (name != null && name == control.tag) {
                            addNote(Consts.Logger.hintTitle,
                                    "A tag in the block of a function cannot receive the ${control.type} control signal")
                        }
                    }
                }
            }
            signalReturnControl -> {
                // Remove Control from the stack.
                analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Control)

                return descriptiveExecutionControllerFinal(analyzer, node)
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }

    private fun descriptiveExecutionControllerFinal(analyzer: LexemAnalyzer, node: ParserNode) {
        val context = AnalyzerCommons.getCurrentContext(analyzer.memory)

        // Check the unions before leave.
        let {
            val unionsRef = context.getPropertyValue(analyzer.memory,
                    AnalyzerCommons.Identifiers.HiddenPatternUnions) as LxmReference
            val unions = unionsRef.dereferenceAs<LxmObject>(analyzer.memory)!!
            for ((_, property) in unions.getAllIterableProperties()) {
                val union = property.value.dereference(analyzer.memory) as LxmPatternUnion

                if (!union.isFinished(analyzer.memory)) {
                    return analyzer.initBacktracking()
                }
            }
        }

        // Get the last point to return to it.
        val lastCodePoint = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint) as LxmCodePoint

        // Get the memory index.
        val memoryIndex = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.LastNode) as LxmBigNode

        // Prepare the node.
        val lxmNodeRef = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.Node)!!
        val lxmNode = lxmNodeRef.dereference(analyzer.memory) as LxmNode
        lxmNode.setTo(analyzer.memory, analyzer.text.saveCursor())

        // Process the properties.
        let {
            val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)

            val children = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Children) ?: LxmNil)
            if (!children) {
                val childList = lxmNode.getChildren(analyzer.memory)
                childList.removeCell(analyzer.memory, childList.listSize)
            }

            var returnValue: LexemPrimitive = lxmNodeRef
            val capture = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Capture) ?: LxmNil)
            if (!capture) {
                val parent = lxmNode.getParent(analyzer.memory)!!
                val parentChildren = parent.getChildren(analyzer.memory)

                // Find position in parent.
                val index = parentChildren.getAllCells()
                        .indexOfFirst { RelationalFunctions.lxmEquals(analyzer.memory, it, lxmNodeRef) }
                if (index < 0) {
                    throw AngmarUnreachableException()
                }

                // Remove from parent.
                parentChildren.removeCell(analyzer.memory, index, ignoreConstant = true)

                // Add children to parent in the same position.
                val childrenArray = lxmNode.getChildren(analyzer.memory).getAllCells().toTypedArray()
                parentChildren.insertCell(analyzer.memory, index, *childrenArray, ignoreConstant = true)

                // Set the returned value.
                if (children && childrenArray.isNotEmpty()) {
                    // Set the children as returned value.
                    val resultList = LxmList()
                    val resultListRef = analyzer.memory.add(resultList)
                    resultList.addCell(analyzer.memory, *childrenArray)
                    returnValue = resultListRef
                } else {
                    // Set a null value.
                    returnValue = LxmNil
                }
            }

            // Set the returned value.
            analyzer.memory.addToStackAsLast(returnValue)

            val propertyValue = props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Property) ?: LxmNil
            val property = RelationalFunctions.isTruthy(propertyValue)
            if (property) {
                val parent = lxmNode.getParent(analyzer.memory)!!
                val parentProps = parent.getProperties(analyzer.memory)

                if (propertyValue is LxmString) {
                    val value = lxmNode.getContent(analyzer.memory)!!
                    parentProps.setProperty(analyzer.memory, propertyValue.primitive, value)
                } else {
                    val value = lxmNode.getContent(analyzer.memory)!!
                    parentProps.setProperty(analyzer.memory, lxmNode.name, value)
                }
            }

            val backtrack = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Backtrack) ?: LxmNil)
            if (!backtrack) {
                analyzer.memory.collapseTo(memoryIndex.node)
            }

            val consume = RelationalFunctions.isTruthy(
                    props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Consume) ?: LxmNil)
            if (!consume) {
                lxmNode.getFrom(analyzer.memory).primitive.restore()
            }
        }

        // Restore the text.
        val oldReader = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.HiddenAnalyzerText)
        if (oldReader != null) {
            (oldReader as LxmReader).restore(analyzer)

            // Change the result.
            val result = analyzer.memory.getLastFromStack().dereference(analyzer.memory)
            when (result) {
                is LxmNode -> {
                    val node2ReParse = context.getPropertyValue(analyzer.memory,
                            AnalyzerCommons.Identifiers.HiddenNode2Filter)!!.dereference(analyzer.memory) as LxmNode

                    result.applyOffset(analyzer.memory, node2ReParse.getFrom(analyzer.memory).primitive)

                    node2ReParse.memoryDeallocBranch(analyzer.memory)
                }
                is LxmList -> {
                    val node2ReParse = context.getPropertyValue(analyzer.memory,
                            AnalyzerCommons.Identifiers.HiddenNode2Filter)!!.dereference(analyzer.memory) as LxmNode
                    val from = node2ReParse.getFrom(analyzer.memory).primitive

                    for (child in result.getAllCells().map { it.dereference(analyzer.memory) as LxmNode }) {
                        child.applyOffset(analyzer.memory, from)
                    }

                    node2ReParse.memoryDeallocBranch(analyzer.memory)
                }
            }
        }

        // Remove the intermediate context.
        AnalyzerCommons.removeCurrentFunctionContextAndAssignPrevious(analyzer.memory)

        // Set parent node as the current one.
        analyzer.setUpperNode()

        // Remove LastNode, Function and ReturnCodePoint from the stack.
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.LastNode)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.Function)
        analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.ReturnCodePoint)

        // Remove FilterNode and FilterNodePosition from the stack.
        if (node is FilterStmtNode) {
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNode)
            analyzer.memory.removeFromStack(AnalyzerCommons.Identifiers.FilterNodePosition)
        }

        return analyzer.nextNode(lastCodePoint)
    }

    /**
     * Resolves the inline properties of lexemes.
     */
    fun resolveInlineProperties(analyzer: LexemAnalyzer, node: LexemPropertyPostfixNode?): Pair<Boolean, Boolean> {
        val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)

        var reversed = RelationalFunctions.isTruthy(
                props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Reverse) ?: LxmNil)
        var insensible = RelationalFunctions.isTruthy(
                props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Insensible) ?: LxmNil)

        val notReversed = !reversed
        val notInsensible = !insensible

        if (node == null) {
            return Pair(reversed, insensible)
        }

        for (p in node.positiveElements) {
            when (p) {
                LexemPropertyPostfixNode.reversedProperty -> reversed = true
                LexemPropertyPostfixNode.insensibleProperty -> insensible = true
            }
        }

        for (p in node.negativeElements) {
            when (p) {
                LexemPropertyPostfixNode.reversedProperty -> reversed = false
                LexemPropertyPostfixNode.insensibleProperty -> insensible = false
            }
        }

        for (p in node.reversedElements) {
            when (p) {
                LexemPropertyPostfixNode.reversedProperty -> reversed = notReversed
                LexemPropertyPostfixNode.insensibleProperty -> insensible = notInsensible
            }
        }

        return Pair(reversed, insensible)
    }
}
