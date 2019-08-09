package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for bitlist literals.
 */
class BitlistNode private constructor(parser: LexemParser) : ParserNode(parser) {
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


    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("radix", radix)
        printer.addField("elements", elements)
    }

    companion object {
        const val startToken = StringNode.startToken
        const val endToken = StringNode.endToken
        const val binaryPrefix = NumberNode.binaryPrefix
        const val octalPrefix = NumberNode.octalPrefix
        const val hexadecimalPrefix = NumberNode.hexadecimalPrefix

        /**
         * Parses a bitlist literal.
         */
        fun parse(parser: LexemParser): BitlistNode? {
            parser.fromBuffer(parser.reader.currentPosition(), BitlistNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = BitlistNode(parser)

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

                val node = BitlistElementNode.parse(parser, result.radix)
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
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the end quote '$endToken' here")
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
