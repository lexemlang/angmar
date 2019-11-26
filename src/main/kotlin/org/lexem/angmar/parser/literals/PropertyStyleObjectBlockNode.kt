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
 * Parser for property-style object blocks.
 */
internal class PropertyStyleObjectBlockNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    val positiveElements = mutableListOf<ParserNode>()
    val negativeElements = mutableListOf<ParserNode>()
    val setElements = mutableListOf<PropertyStyleObjectElementNode>()

    override fun toString() = StringBuilder().apply {
        append(startToken)

        if (positiveElements.isNotEmpty()) {
            append(positiveElements.joinToString(" "))
        }

        if (negativeElements.isNotEmpty()) {
            append(negativeToken)
            append(' ')
            append(negativeElements.joinToString(" "))
        }

        if (setElements.isNotEmpty()) {
            append(setToken)
            append(' ')
            append(setElements.joinToString(" "))
        }
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("positiveElements", SerializationUtils.listToTest(positiveElements))
        result.add("negativeElements", SerializationUtils.listToTest(negativeElements))
        result.add("setElements", SerializationUtils.listToTest(setElements))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyStyleObjectBlockAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = "["
        const val negativeToken = "-"
        const val setToken = ":"
        const val endToken = "]"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a property-style object blocks
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): PropertyStyleObjectBlockNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PropertyStyleObjectBlockNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = PropertyStyleObjectBlockNode(parser, parent, parentSignal)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val element = Commons.parseDynamicIdentifier(parser, result,
                        result.positiveElements.size + PropertyStyleObjectBlockAnalyzer.signalEndFirstElement)
                if (element == null) {
                    initLoopCursor.restore()
                    break
                }

                result.positiveElements.add(element)
            }

            let {
                val initNegativeCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (parser.readText(negativeToken)) {
                    while (true) {
                        val initLoopCursor = parser.reader.saveCursor()

                        WhitespaceNode.parse(parser)

                        val element = Commons.parseDynamicIdentifier(parser, result,
                                result.positiveElements.size + result.negativeElements.size + PropertyStyleObjectBlockAnalyzer.signalEndFirstElement)
                        if (element == null) {
                            initLoopCursor.restore()
                            break
                        }

                        result.negativeElements.add(element)
                    }
                } else {
                    initNegativeCursor.restore()
                }
            }

            let {
                val initSetCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (parser.readText(setToken)) {
                    while (true) {
                        val initLoopCursor = parser.reader.saveCursor()

                        WhitespaceNode.parse(parser)

                        val element = PropertyStyleObjectElementNode.parse(parser, result,
                                result.positiveElements.size + result.negativeElements.size + result.setElements.size + PropertyStyleObjectBlockAnalyzer.signalEndFirstElement)
                        if (element == null) {
                            initLoopCursor.restore()
                            break
                        }

                        result.setElements.add(element)
                    }
                } else {
                    initSetCursor.restore()
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.PropertyStyleObjectBlockWithoutEndToken,
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
