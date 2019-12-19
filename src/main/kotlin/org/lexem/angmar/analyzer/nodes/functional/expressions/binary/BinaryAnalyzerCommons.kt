package org.lexem.angmar.analyzer.nodes.functional.expressions.binary

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.modifiers.*


/**
 * Generic analyzer commons for binary expressions.
 */
internal object BinaryAnalyzerCommons {
    /**
     * Gets the function of an operator.
     */
    fun getOperatorFunction(analyzer: LexemAnalyzer, valueRef: LexemPrimitive, expressionNode: ParserNode,
            leftOperand: ParserNode, operator: String, operatorFunctionName: String): LxmFunction {
        val value = valueRef.dereference(analyzer.memory, toWrite = false)
        val prototype = value.getObjectOrPrototype(analyzer.memory, toWrite = false)

        val operatorFunctionRef = prototype.getPropertyValue(analyzer.memory, operatorFunctionName)
        val operatorFunction =
                operatorFunctionRef?.dereference(analyzer.memory, toWrite = false) ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.UndefinedObjectProperty,
                        "The operator $operator is associated to a function called '$operatorFunctionName' which is not contained inside the prototype of the operand. Current: $prototype") {
                    val fullText = expressionNode.parser.reader.readAllText()
                    addSourceCode(fullText, expressionNode.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(expressionNode.from.position(), expressionNode.to.position() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(expressionNode.from.position(), leftOperand.to.position() - 1)
                        message =
                                "The returned value by this expression has not a property called '$operatorFunctionName'"
                    }
                }

        if (operatorFunction !is LxmFunction) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.IncompatibleType,
                    "The operator $operator is associated to a function called '$operatorFunctionName' but the returned value is not callable, i.e. a function or expression. Current: $operatorFunction") {
                val fullText = expressionNode.parser.reader.readAllText()
                addSourceCode(fullText, expressionNode.parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(expressionNode.from.position(), expressionNode.to.position() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(expressionNode.from.position(), leftOperand.to.position() - 1)
                    message =
                            "The property called '$operatorFunctionName' of the returned value by this expression is not callable."
                }
            }
        }

        return operatorFunction
    }

    /**
     * Creates an arguments object for an operator function.
     */
    fun createArguments(analyzer: LexemAnalyzer, left: LexemMemoryValue, right: LexemMemoryValue): LxmArguments {
        val arguments = LxmArguments(analyzer.memory)
        arguments.addPositionalArgument(analyzer.memory, right)

        arguments.addNamedArgument(analyzer.memory, AnalyzerCommons.Identifiers.This, left)

        return arguments
    }

    /**
     * Executes a unitary operator.
     */
    inline fun <reified T : LexemMemoryValue> executeUnitaryOperator(analyzer: LexemAnalyzer, arguments: LxmArguments,
            functionName: String, thisTypeName: String, toWrite: Boolean,
            processFunction: (LexemAnalyzer, T) -> LexemMemoryValue): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, emptyList())

        val thisValue =
                parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWrite) ?: LxmNil

        if (thisValue !is T) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<$thisTypeName value>${AccessExplicitMemberNode.accessToken}$functionName' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a $thisTypeName") {}
        }

        val result = processFunction(analyzer, thisValue)

        analyzer.memory.addToStackAsLast(result)
        return true
    }

    /**
     * Executes a binary operator.
     */
    inline fun <reified T : LexemMemoryValue> executeBinaryOperator(analyzer: LexemAnalyzer, arguments: LxmArguments,
            functionName: String, leftTypeName: String, typeNamesForRightOperand: List<String>, toWriteLeft: Boolean,
            toWriteRight: Boolean,
            processFunction: (LexemAnalyzer, T, LexemMemoryValue) -> LexemMemoryValue?): Boolean {
        val parserArguments = arguments.mapArguments(analyzer.memory, AnalyzerCommons.Operators.ParameterList)

        val left =
                parserArguments[AnalyzerCommons.Identifiers.This]?.dereference(analyzer.memory, toWriteLeft) ?: LxmNil
        val right = parserArguments[AnalyzerCommons.Operators.RightParameterName]?.dereference(analyzer.memory,
                toWriteRight) ?: LxmNil

        if (left !is T) {
            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadThisArgumentTypeError,
                    "The '<$leftTypeName value>${AccessExplicitMemberNode.accessToken}$functionName' method requires the parameter called '${AnalyzerCommons.Identifiers.This}' be a $leftTypeName") {}
        }

        val result = processFunction(analyzer, left, right) ?: let {
            var message =
                    "The '<$leftTypeName value>${AccessExplicitMemberNode.accessToken}$functionName' method requires the parameter called '${AnalyzerCommons.Operators.RightParameterName}' be a "

            if (typeNamesForRightOperand.size == 1) {
                message += typeNamesForRightOperand.last()
            } else {
                message += typeNamesForRightOperand.dropLast(1).joinToString(", a ")
                message += " or a ${typeNamesForRightOperand.last()}"
            }

            throw AngmarAnalyzerException(AngmarAnalyzerExceptionType.BadArgumentError, message) {}
        }

        analyzer.memory.addToStackAsLast(result)
        return true
    }
}
