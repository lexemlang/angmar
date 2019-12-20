package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for elements of destructuring.
 */
internal class DestructuringElementStmtNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var alias: IdentifierNode
    var isConstant = false
    var original: IdentifierNode? = null

    override fun toString() = StringBuilder().apply {
        if (original != null) {
            append(original)
            append(' ')
            append(aliasToken)
            append(' ')
        }
        if (isConstant) {
            append(constantToken)
        }
        append(alias)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isConstant", isConstant)
        result.add("original", original?.toTree())
        result.add("alias", alias.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            DestructuringElementStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val aliasToken = "as"
        const val constantToken = GlobalCommons.constantToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses for an element of destructuring.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): DestructuringElementStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = DestructuringElementStmtNode(parser, parent, parentSignal)

            // original
            let {
                val initOriginalCursor = parser.reader.saveCursor()

                val ori = IdentifierNode.parse(parser, result, DestructuringElementStmtAnalyzer.signalEndOriginal)
                        ?: return@let

                WhitespaceNode.parse(parser)

                if (!parser.readText(aliasToken)) {
                    initOriginalCursor.restore()
                    return@let
                }

                WhitespaceNode.parse(parser)

                result.original = ori
            }

            result.isConstant = parser.readText(constantToken)

            result.alias =
                    IdentifierNode.parse(parser, result, DestructuringElementStmtAnalyzer.signalEndAlias) ?: let {
                        if (result.original != null) {
                            throw AngmarParserException(
                                    AngmarParserExceptionType.DestructuringElementStatementWithoutIdentifierAfterAliasToken,
                                    "An identifier was expected after the alias operator '$aliasToken'.") {
                                val fullText = parser.reader.readAllText()
                                addSourceCode(fullText, parser.reader.getSource()) {
                                    title = Consts.Logger.codeTitle
                                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                                }
                                addSourceCode(fullText, null) {
                                    title = Consts.Logger.hintTitle
                                    highlightCursorAt(parser.reader.currentPosition())
                                    message = "Try adding a whitespace followed by an identifier here"
                                }
                            }
                        }

                        initCursor.restore()
                        return null
                    }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
