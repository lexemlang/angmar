package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for unicode interval literals.
 */
internal class UnicodeIntervalNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
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

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            UnicodeIntervalAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val macroName = "uitv${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a unicode interval literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): UnicodeIntervalNode? {
            parser.fromBuffer(parser.reader.currentPosition(), UnicodeIntervalNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = UnicodeIntervalNode(parser, parent, parentSignal)

            if (!parser.readText(macroName)) {
                return null
            }

            result.node = UnicodeIntervalAbbrNode.parse(parser, result, UnicodeIntervalAnalyzer.signalEndNode)
                    ?: throw AngmarParserException(AngmarParserExceptionType.UnicodeIntervalWithoutStartToken,
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
