package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for function parameters.
 */
internal class FunctionParameterNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var identifier: ParserNode
    var expression: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(identifier)
        if (expression != null) {
            append(assignOperator)
            append(expression)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("identifier", identifier.toTree())
        result.add("expression", expression?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionParameterAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val assignOperator = AssignOperatorNode.assignOperator

        // METHODS ------------------------------------------------------------

        /**
         * Parses a function parameter.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): FunctionParameterNode? {
            val initCursor = parser.reader.saveCursor()
            val result = FunctionParameterNode(parser, parent, parentSignal)

            result.identifier =
                    Commons.parseDynamicIdentifier(parser, result, FunctionParameterAnalyzer.signalEndIdentifier)
                            ?: return null

            // Assign
            let {
                val initAssignCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!parser.readText(assignOperator)) {
                    initAssignCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.expression = ExpressionsCommons.parseExpression(parser, result,
                        FunctionParameterAnalyzer.signalEndExpression) ?: throw AngmarParserException(
                        AngmarParserExceptionType.FunctionParameterWithoutExpressionAfterAssignOperator,
                        "An expression was expected after the assign operator '$assignOperator' to act as the default value of the function parameter.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding an expression here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
