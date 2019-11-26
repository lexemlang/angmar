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
 * Parser for bitlist literals.
 */
internal class BitlistNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var radix = 2
    val elements = mutableListOf<BitlistElementNode>()

    override fun toString() = StringBuilder().apply {
        when (radix) {
            2 -> append(binaryPrefix)
            8 -> append(octalPrefix)
            16 -> append(hexadecimalPrefix)
        }

        append(startToken)
        append(elements)
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("radix", radix.toLong())
        result.add("elements", SerializationUtils.listToTest(elements))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = BitListAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val startToken = StringNode.startToken
        const val endToken = StringNode.endToken
        const val binaryPrefix = NumberNode.binaryPrefix
        const val octalPrefix = NumberNode.octalPrefix
        const val hexadecimalPrefix = NumberNode.hexadecimalPrefix

        /**
         * Parses a bitlist literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): BitlistNode? {
            parser.fromBuffer(parser.reader.currentPosition(), BitlistNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = BitlistNode(parser, parent, parentSignal)

            result.radix = when {
                parser.readText(binaryPrefix) -> 2
                parser.readText(octalPrefix) -> 8
                parser.readText(hexadecimalPrefix) -> 16
                else -> return null
            }

            if (!parser.readText(startToken)) {
                initCursor.restore()
                return null
            }

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parseSimpleWhitespaces(parser)

                val node = BitlistElementNode.parse(parser, result,
                        result.elements.size + BitListAnalyzer.signalEndFirstElement, result.radix)
                if (node == null) {
                    initLoopCursor.restore()
                    break
                }

                result.elements.add(node)
            }

            WhitespaceNode.parseSimpleWhitespaces(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.BitlistWithoutEndToken,
                        "Bitlist literals require the end quote '$endToken' to finish the literal.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the end quote '$endToken' here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
