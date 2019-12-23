package org.lexem.angmar.parser.functional.expressions.modifiers

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.modifiers.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for expression properties of function calls.
 */
internal class FunctionCallExpressionPropertiesNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var value: PropertyStyleObjectBlockNode

    override fun toString() = StringBuilder().apply {
        append(relationalToken)
        append(value)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("value", value.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            FunctionCallExpressionPropertiesCompiled.compile(parent, parentSignal, this)

    companion object {
        const val relationalToken = GlobalCommons.relationalToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses an expression properties of function calls.
         */
        fun parse(parser: LexemParser, parent: ParserNode): FunctionCallExpressionPropertiesNode? {
            val initCursor = parser.reader.saveCursor()
            val result = FunctionCallExpressionPropertiesNode(parser, parent)

            if (!parser.readText(relationalToken)) {
                return null
            }

            result.value = PropertyStyleObjectBlockNode.parse(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.FunctionCallExpressionPropertiesWithoutPropertyStyleBlockAfterRelationalToken,
                    "A property-style block was expected after the relational token '$relationalToken'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message =
                            "Try adding an empty property-style block here '${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.endToken}'"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
