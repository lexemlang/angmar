package org.lexem.angmar.parser.functional.expressions.modifiers

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.modifiers.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for indexer i.e. element\[access]
 */
internal class IndexerNode private constructor(parser: LexemParser, parent: ParserNode) : ParserNode(parser, parent) {
    lateinit var expression: ParserNode

    override fun toString() = "$startToken$expression$endToken"

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("expression", expression.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) = IndexerCompiled.compile(parent, parentSignal, this)

    companion object {
        const val startToken = "["
        const val endToken = "]"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an indexer expression
         */
        fun parse(parser: LexemParser, parent: ParserNode): IndexerNode? {
            val initCursor = parser.reader.saveCursor()
            val result = IndexerNode(parser, parent)

            if (!parser.readText(startToken)) {
                return null
            }

            WhitespaceNode.parse(parser)

            result.expression = ExpressionsCommons.parseExpression(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.IndexerWithoutStartToken,
                    "An expression was expected after the open square bracket '$startToken'.") {
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

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.IndexerWithoutEndToken,
                        "The close square bracket was expected '$endToken'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close square bracket '$endToken' here"
                    }
                }

            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
