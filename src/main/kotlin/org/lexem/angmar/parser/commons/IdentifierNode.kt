package org.lexem.angmar.parser.commons

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.commons.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.binary.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for identifiers.
 */
internal class IdentifierNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    val simpleIdentifiers = mutableListOf<String>()
    var quotedIdentifier: QuotedIdentifierNode? = null

    val isQuotedIdentifier get() = quotedIdentifier != null

    override fun toString() = if (isQuotedIdentifier) {
        quotedIdentifier.toString()
    } else {
        simpleIdentifiers.joinToString(middleChar)
    }

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("simpleIdentifiers", SerializationUtils.stringListToTest(simpleIdentifiers))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = IdentifierAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        val headChars = listOf('\u0041'..'\u005A', '\u005F'..'\u005F', '\u0061'..'\u007A', '\u00A8'..'\u00A8',
                '\u00AA'..'\u00AA', '\u00AD'..'\u00AD', '\u00AF'..'\u00AF', '\u00B2'..'\u00B5', '\u00B7'..'\u00BA',
                '\u00BC'..'\u00BE', '\u00C0'..'\u00D6', '\u00D8'..'\u00F6', '\u00F8'..'\u00FF', '\u0100'..'\u02FF',
                '\u0370'..'\u167F', '\u1681'..'\u180D', '\u180F'..'\u1DBF', '\u1E00'..'\u1FFF', '\u200B'..'\u200D',
                '\u202A'..'\u202E', '\u203F'..'\u2040', '\u2054'..'\u2054', '\u2060'..'\u206F', '\u2070'..'\u20CF',
                '\u2100'..'\u218F', '\u2460'..'\u24FF', '\u2776'..'\u2793', '\u2C00'..'\u2DFF', '\u2E80'..'\u2FFF',
                '\u3004'..'\u3007', '\u3021'..'\u302F', '\u3031'..'\u303F', '\u3040'..'\uD7FF', '\uF900'..'\uFD3D',
                '\uFD40'..'\uFDCF', '\uFDF0'..'\uFDFF', '\uFE10'..'\uFE1F', '\uFE30'..'\uFE44', '\uFE47'..'\uFFFD')
        val endChars = listOf('\u0030'..'\u0039', '\u0300'..'\u036F', '\u1DC0'..'\u1DFF', '\u20D0'..'\u20FF',
                '\uFE20'..'\uFE2F')
        const val middleChar = "-"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an identifier.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): IdentifierNode? {
            parser.fromBuffer(parser.reader.currentPosition(), IdentifierNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = IdentifierNode(parser, parent, parentSignal)

            result.quotedIdentifier =
                    QuotedIdentifierNode.parse(parser, result, IdentifierAnalyzer.signalEndQuotedIdentifier)
            if (result.quotedIdentifier != null) {
                return parser.finalizeNode(result, initCursor)
            }

            result.simpleIdentifiers.add(parseSimpleIdentifier(parser) ?: return null)

            while (true) {
                val preDashTokenCursor = parser.reader.saveCursor()

                if (!parser.readText(middleChar)) {
                    break
                }

                val simpleIdentifier = parseSimpleIdentifier(parser)
                if (simpleIdentifier == null) {
                    preDashTokenCursor.restore()
                    break
                } else {
                    result.simpleIdentifiers.add(simpleIdentifier)
                }
            }

            if (result.simpleIdentifiers.isEmpty()) {
                return null
            }

            // Avoid confusing a macro with an identifier but preventing the != operator.
            if (!parser.checkText(RelationalExpressionNode.inequalityOperator) && parser.readText(
                            MacroExpressionNode.macroSuffix)) {
                initCursor.restore()
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a simple identifier. i.e. without dash tokens '-'.
         */
        private fun parseSimpleIdentifier(parser: LexemParser): String? {
            val result = StringBuilder()
            result.append(parser.readAnyChar(headChars) ?: return null)

            while (true) {
                result.append(parser.readAnyChar(headChars) ?: parser.readAnyChar(endChars) ?: return result.toString())
            }
        }
    }
}
