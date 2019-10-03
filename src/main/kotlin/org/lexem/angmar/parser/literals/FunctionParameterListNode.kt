package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for function parameter list.
 */
internal class FunctionParameterListNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    val parameters = mutableListOf<FunctionParameterNode>()
    var positionalSpread: IdentifierNode? = null
    var namedSpread: IdentifierNode? = null

    override fun toString() = StringBuilder().apply {
        append(startToken)
        append(parameters.joinToString("$parameterSeparator "))

        if (positionalSpread != null) {
            if (parameters.isNotEmpty()) {
                append("$parameterSeparator ")
            }
            append(positionalSpreadOperator)
            append(namedSpread)
        }

        if (namedSpread != null) {
            if (parameters.isNotEmpty() || positionalSpread != null) {
                append("$parameterSeparator ")
            }
            append(namedSpreadOperator)
            append(namedSpread)
        }

        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("parameters", TreeLikePrintable.listToTest(parameters))
        result.add("positionalSpread", positionalSpread?.toTree())
        result.add("namedSpread", namedSpread?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            FunctionParameterListAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "("
        const val parameterSeparator = GlobalCommons.elementSeparator
        const val positionalSpreadOperator = GlobalCommons.spreadOperator
        const val namedSpreadOperator = "$positionalSpreadOperator@"
        const val endToken = ")"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a function parameter list.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): FunctionParameterListNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionParameterListNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = FunctionParameterListNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            var parameter = FunctionParameterNode.parse(parser, result,
                    result.parameters.size + FunctionParameterListAnalyzer.signalEndFirstParameter)
            if (parameter != null) {
                result.parameters.add(parameter)

                while (true) {
                    val initLoopCursor = parser.reader.saveCursor()

                    WhitespaceNode.parse(parser)

                    if (!parser.readText(parameterSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)

                    parameter = FunctionParameterNode.parse(parser, result,
                            result.parameters.size + FunctionParameterListAnalyzer.signalEndFirstParameter)
                    if (parameter == null) {
                        initLoopCursor.restore()
                        break
                    }

                    result.parameters.add(parameter)
                }
            }

            // Positional spread.
            let {
                val initSpreadCursor = parser.reader.saveCursor()

                if (result.parameters.isNotEmpty()) {
                    WhitespaceNode.parse(parser)

                    if (!parser.readText(parameterSeparator)) {
                        initSpreadCursor.restore()
                        return@let
                    }

                    WhitespaceNode.parse(parser)
                }

                if (parser.checkText(namedSpreadOperator) || !parser.readText(positionalSpreadOperator)) {
                    initSpreadCursor.restore()
                    return@let
                }

                result.positionalSpread =
                        IdentifierNode.parse(parser, result, FunctionParameterListAnalyzer.signalEndPositionalSpread)
                                ?: throw AngmarParserException(
                                        AngmarParserExceptionType.FunctionParameterListWithoutIdentifierAfterPositionalSpreadOperator,
                                        "An identifier was expected after the spread operator '$positionalSpreadOperator'.") {
                                    val fullText = parser.reader.readAllText()
                                    addSourceCode(fullText, parser.reader.getSource()) {
                                        title = Consts.Logger.codeTitle
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(fullText, null) {
                                        title = Consts.Logger.hintTitle
                                        highlightCursorAt(parser.reader.currentPosition())
                                        message = "Try adding an identifier here"
                                    }
                                }
            }

            // Named spread.
            let {
                val initSpreadCursor = parser.reader.saveCursor()

                if (result.parameters.isNotEmpty() || result.positionalSpread != null) {
                    WhitespaceNode.parse(parser)

                    if (!parser.readText(parameterSeparator)) {
                        initSpreadCursor.restore()
                        return@let
                    }

                    WhitespaceNode.parse(parser)
                }

                if (!parser.readText(namedSpreadOperator)) {
                    initSpreadCursor.restore()
                    return@let
                }

                result.namedSpread =
                        IdentifierNode.parse(parser, result, FunctionParameterListAnalyzer.signalEndNamedSpread)
                                ?: throw AngmarParserException(
                                        AngmarParserExceptionType.FunctionParameterListWithoutIdentifierAfterNamedSpreadOperator,
                                        "An identifier was expected after the spread operator '$namedSpreadOperator'.") {
                                    val fullText = parser.reader.readAllText()
                                    addSourceCode(fullText, parser.reader.getSource()) {
                                        title = Consts.Logger.codeTitle
                                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                    }
                                    addSourceCode(fullText, null) {
                                        title = Consts.Logger.hintTitle
                                        highlightCursorAt(parser.reader.currentPosition())
                                        message = "Try adding an identifier here"
                                    }
                                }
            }

            // Trailing comma.
            let {
                val initTrailingCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!parser.readText(parameterSeparator)) {
                    initTrailingCursor.restore()
                    return@let
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.FunctionParameterListWithoutEndToken,
                        "The close parenthesis was expected '$endToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close parenthesis '$endToken' here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
