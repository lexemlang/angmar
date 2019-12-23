package org.lexem.angmar.parser.functional.expressions

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.expressions.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*


/**
 * Parser for prefix expression.
 */
internal class PrefixExpressionNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var prefix: PrefixOperatorNode
    lateinit var expression: ParserNode

    override fun toString() = StringBuilder().apply {
        append(prefix)
        append(expression)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("macroName", prefix.toTree())
        result.add("expression", expression.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            PrefixExpressionCompiled.compile(parent, parentSignal, this)

    companion object {
        /**
         * Parses a prefix expression.
         */
        fun parse(parser: LexemParser, parent: ParserNode): ParserNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PrefixExpressionNode(parser, parent)

            val prefix = PrefixOperatorNode.parse(parser, result)
            result.expression = AccessExpressionNode.parse(parser, result) ?: let {
                if (prefix == null) {
                    return@parse null
                }

                throw AngmarParserException(
                        AngmarParserExceptionType.PrefixExpressionWithoutExpressionAfterThePrefixOperator,
                        "An infix expression was expected after the prefix operator '$prefix'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(prefix.from.position(), prefix.to.position() - 1)
                        message = "Try removing the prefix operator"
                    }
                }
            }

            if (prefix == null) {
                val newResult = result.expression
                newResult.parent = parent
                return newResult
            }

            result.prefix = prefix

            return parser.finalizeNode(result, initCursor)
        }
    }
}
