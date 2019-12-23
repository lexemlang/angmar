package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*


/**
 * Parser for property-style objects.
 */
internal class PropertyStyleObjectNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var block: PropertyStyleObjectBlockNode
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        append(startToken)

        if (isConstant) {
            append(constantToken)
        }

        append(block)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isConstant", isConstant)
        result.add("block", block.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            PropertyStyleObjectCompiled.compile(parent, parentSignal, this)

    companion object {
        const val startToken = "@"
        const val constantToken = GlobalCommons.constantToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses a property-style object.
         */
        fun parse(parser: LexemParser, parent: ParserNode): PropertyStyleObjectNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PropertyStyleObjectNode(parser, parent)

            if (!parser.readText(startToken)) {
                return null
            }

            result.isConstant = parser.readText(constantToken)
            result.block = PropertyStyleObjectBlockNode.parse(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.PropertyStyleObjectWithoutStartToken,
                    "The open square bracket was expected '${PropertyStyleObjectBlockNode.startToken}'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message = "Try adding the open square bracket '${PropertyStyleObjectBlockNode.startToken}' here"
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    message = "Try removing the start token '$startToken'"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
