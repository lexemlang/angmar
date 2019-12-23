package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for unicode interval literals.
 */
internal class UnicodeIntervalNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var node: UnicodeIntervalAbbrNode

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(node)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("node", node.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) = node.compile(parent, parentSignal)

    companion object {
        const val macroName = "uitv${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a unicode interval literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode): UnicodeIntervalNode? {
            val initCursor = parser.reader.saveCursor()
            val result = UnicodeIntervalNode(parser, parent)

            if (!parser.readText(macroName)) {
                return null
            }

            result.node = UnicodeIntervalAbbrNode.parse(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.UnicodeIntervalWithoutStartToken,
                    "The start square bracket was expected '${UnicodeIntervalAbbrNode.startToken}'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message =
                            "Try adding the start and end square brackets '${UnicodeIntervalAbbrNode.startToken}${UnicodeIntervalAbbrNode.endToken}' here"
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message = "Try removing the '$macroName' macro"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
