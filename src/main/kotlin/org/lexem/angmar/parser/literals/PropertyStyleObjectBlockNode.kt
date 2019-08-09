package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for property-style object blocks.
 */
class PropertyStyleObjectBlockNode private constructor(parser: LexemParser) : ParserNode(parser) {
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

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("positiveElements", positiveElements)
        printer.addField("negativeElements", negativeElements)
        printer.addField("setElements", setElements)
    }

    companion object {
        const val startToken = "["
        const val negativeToken = "-"
        const val setToken = ":"
        const val endToken = "]"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a property-style object blocks
         */
        fun parse(parser: LexemParser): PropertyStyleObjectBlockNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PropertyStyleObjectBlockNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = PropertyStyleObjectBlockNode(parser)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val element = Commons.parseDynamicIdentifier(parser)
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

                        val element = Commons.parseDynamicIdentifier(parser)
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
                val initsetCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                if (parser.readText(setToken)) {
                    while (true) {
                        val initLoopCursor = parser.reader.saveCursor()

                        WhitespaceNode.parse(parser)

                        val element = PropertyStyleObjectElementNode.parse(parser)
                        if (element == null) {
                            initLoopCursor.restore()
                            break
                        }

                        result.setElements.add(element)
                    }
                } else {
                    initsetCursor.restore()
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.PropertyStyleObjectBlockWithoutEndToken,
                        "The close square bracket was expected '$endToken'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the close square bracket '$endToken' here")
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
