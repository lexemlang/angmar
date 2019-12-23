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
 * Parser for sub-intervals of interval literals.
 */
internal class IntervalSubIntervalNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var operator = Operator.Add
    var reversed = false
    val elements = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(startToken)
        append(operator.operator)
        if (reversed) {
            append(reversedToken)
        }
        append(elements.joinToString(" "))
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("elements", SerializationUtils.listToTest(elements))
        result.addProperty("reversed", reversed)

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            IntervalSubIntervalCompiled.compile(parent, parentSignal, this)

    companion object {
        const val startToken = "["
        const val reversedToken = GlobalCommons.notToken
        const val endToken = "]"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a sub-interval of interval literals.
         */
        fun parse(parser: LexemParser, parent: ParserNode): IntervalSubIntervalNode? {
            val initCursor = parser.reader.saveCursor()
            val result = IntervalSubIntervalNode(parser, parent)

            if (!parser.readText(startToken)) {
                return null
            }

            val operator = when {
                parser.readText(Operator.Add.operator) -> Operator.Add
                parser.readText(Operator.Sub.operator) -> Operator.Sub
                parser.readText(Operator.Common.operator) -> Operator.Common
                parser.readText(Operator.NotCommon.operator) -> Operator.NotCommon
                else -> null
            }

            if (operator != null) {
                result.operator = operator
                result.reversed = parser.readText(reversedToken)
            }

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parseSimpleWhitespaces(parser)

                val node = parse(parser, result) ?: IntervalElementNode.parse(parser, result)
                if (node == null) {
                    initLoopCursor.restore()
                    break
                }

                result.elements.add(node)
            }

            WhitespaceNode.parseSimpleWhitespaces(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.IntervalSubIntervalWithoutEndToken,
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

    internal enum class Operator(val operator: String) {
        Add("+"),
        Sub("-"),
        Common("&"),
        NotCommon("^")
    }
}
