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


/**
 * Parser for object literals.
 */
internal class ObjectNode private constructor(parser: LexemParser, parent: ParserNode) : ParserNode(parser, parent) {
    val elements = mutableListOf<ParserNode>()
    var isConstant = false

    override fun toString() = StringBuilder().apply {
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

    override fun compile(parent: CompiledNode, parentSignal: Int) = ObjectCompiled.compile(parent, parentSignal, this)

    companion object {
        const val startToken = "{"
        const val constantToken = GlobalCommons.constantToken
        const val elementSeparator = GlobalCommons.elementSeparator
        const val endToken = "}"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a object literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode): ObjectNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ObjectNode(parser, parent)

            result.isConstant = parser.readText(constantToken)

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
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

                val argument = ObjectSimplificationNode.parse(parser, result) ?: ObjectElementNode.parse(parser, result)
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
                // If it is empty it return because can collide with a block.
                if (result.elements.isEmpty()) {
                    initCursor.restore()
                    return null
                }
                throw AngmarParserException(AngmarParserExceptionType.ObjectWithoutEndToken,
                        "The close bracket '$endToken' was expected to finish the object literal.") {
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
