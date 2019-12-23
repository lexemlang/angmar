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
 * Parser for named arguments of function calls.
 */
internal class FunctionCallNamedArgumentNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var identifier: ParserNode
    lateinit var expression: ParserNode

    override fun toString() = StringBuilder().apply {
        append(identifier)
        append(relationalToken)
        append(expression)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("identifier", identifier.toTree())
        result.add("expression", expression.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            FunctionCallNamedArgumentCompiled.compile(parent, parentSignal, this)

    companion object {
        const val relationalToken = GlobalCommons.relationalToken

        // METHODS ------------------------------------------------------------

        /**
         * Parses a named argument of a function call.
         */
        fun parse(parser: LexemParser, parent: ParserNode): FunctionCallNamedArgumentNode? {
            val initCursor = parser.reader.saveCursor()
            val result = FunctionCallNamedArgumentNode(parser, parent)

            result.identifier = Commons.parseDynamicIdentifier(parser, result) ?: return null

            WhitespaceNode.parse(parser)

            if (!parser.readText(relationalToken)) {
                initCursor.restore()
                return null
            }

            WhitespaceNode.parse(parser)

            result.expression = ExpressionsCommons.parseExpression(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.FunctionCallMiddleArgumentWithoutExpressionAfterRelationalToken,
                    "An expression was expected after the relational token '$relationalToken'.") {
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

            return parser.finalizeNode(result, initCursor)
        }
    }
}
