package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for destructuring.
 */
internal class DestructuringStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var alias: IdentifierNode? = null
    val elements = mutableListOf<DestructuringElementStmtNode>()
    var spread: DestructuringSpreadStmtNode? = null

    override fun toString() = StringBuilder().apply {
        if (alias != null) {
            append(alias)
            append("$elementSeparator ")
        }
        append(startToken)
        append(elements.joinToString("$elementSeparator "))
        if (spread != null) {
            if (elements.isNotEmpty()) {
                append("$elementSeparator ")
            }
            append(spread)
        }
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("alias", alias?.toTree())
        result.add("elements", SerializationUtils.listToTest(elements))
        result.add("spread", spread?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            DestructuringStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "("
        const val elementSeparator = GlobalCommons.elementSeparator
        const val endToken = ")"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a destructuring.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): DestructuringStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = DestructuringStmtNode(parser, parent, parentSignal)

            result.alias = IdentifierNode.parse(parser, result, DestructuringStmtAnalyzer.signalEndAlias)

            if (result.alias != null) {
                WhitespaceNode.parse(parser)

                if (!parser.readText(elementSeparator)) {
                    initCursor.restore()
                    return null
                }

                WhitespaceNode.parse(parser)
            }

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            var element = DestructuringElementStmtNode.parse(parser, result,
                    result.elements.size + DestructuringStmtAnalyzer.signalEndFirstElement)
            if (element != null) {
                result.elements.add(element)

                while (true) {
                    val initLoopCursor = parser.reader.saveCursor()

                    WhitespaceNode.parse(parser)

                    if (!parser.readText(elementSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)

                    element = DestructuringElementStmtNode.parse(parser, result,
                            result.elements.size + DestructuringStmtAnalyzer.signalEndFirstElement)
                    if (element == null) {
                        initLoopCursor.restore()
                        break
                    }

                    result.elements.add(element)
                }
            }

            // Spread
            let {
                val initSpreadCursor = parser.reader.saveCursor()

                if (result.elements.isNotEmpty()) {
                    WhitespaceNode.parse(parser)

                    if (!parser.readText(elementSeparator)) {
                        initSpreadCursor.restore()
                        return@let
                    }

                    WhitespaceNode.parse(parser)
                }

                result.spread =
                        DestructuringSpreadStmtNode.parse(parser, result, DestructuringStmtAnalyzer.signalEndSpread)
                if (result.spread == null) {
                    initSpreadCursor.restore()
                }
            }

            // Trailing comma
            let {
                val initTrailingCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (!parser.readText(elementSeparator)) {
                    initTrailingCursor.restore()
                    return@let
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.DestructuringStatementWithoutEndToken,
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
