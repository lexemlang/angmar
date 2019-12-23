package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for map literals.
 */
internal class MapNode private constructor(parser: LexemParser, parent: ParserNode) : ParserNode(parser, parent) {
    val elements = mutableListOf<MapElementNode>()
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        append(macroName)
        if (isConstant) {
            append(constantToken)
        }
        append(startToken)
        append(elements.joinToString("$elementSeparator "))
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isConstant", isConstant)
        result.add("elements", SerializationUtils.listToTest(elements))

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) = MapCompiled.compile(parent, parentSignal, this)

    companion object {
        const val macroName = "map${MacroExpressionNode.macroSuffix}"
        const val startToken = "{"
        const val constantToken = GlobalCommons.constantToken
        const val elementSeparator = GlobalCommons.elementSeparator
        const val endToken = "}"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a map literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode): MapNode? {
            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(macroName)) {
                return null
            }

            val result = MapNode(parser, parent)
            result.isConstant = parser.readText(constantToken)

            if (!parser.readText(startToken)) {
                throw AngmarParserException(AngmarParserExceptionType.MapWithoutStartToken,
                        "The opening bracket '$startToken' was expected after the macro name '$macroName'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        message = "Try removing the macro '$macroName'"
                    }
                }
            }

            WhitespaceNode.parse(parser)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                if (result.elements.isNotEmpty()) {
                    WhitespaceNode.parse(parser)

                    if (!parser.readText(elementSeparator)) {
                        initLoopCursor.restore()
                        break
                    }

                    WhitespaceNode.parse(parser)
                }

                val argument = MapElementNode.parse(parser, result)
                if (argument == null) {
                    initLoopCursor.restore()
                    break
                }

                result.elements.add(argument)
            }

            // Trailing comma.
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
                throw AngmarParserException(AngmarParserExceptionType.MapWithoutEndToken,
                        "The close bracket '$endToken' was expected to finish the map literal.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close bracket '$endToken' here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
