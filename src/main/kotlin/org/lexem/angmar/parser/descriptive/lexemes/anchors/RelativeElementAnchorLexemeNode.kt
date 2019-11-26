package org.lexem.angmar.parser.descriptive.lexemes.anchors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for relative element of anchor lexemes.
 */
internal class RelativeElementAnchorLexemeNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var type: RelativeAnchorType
    var value: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        append(type.identifier)
        if (value != null) {
            append(value)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("type", type.identifier)
        result.add("value", value?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            RelativeElementAnchorLexemeAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val textIdentifier = "text"
        const val lineIdentifier = "line"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a relative element of an anchor lexemes.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): RelativeElementAnchorLexemeNode? {
            parser.fromBuffer(parser.reader.currentPosition(), RelativeElementAnchorLexemeNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = RelativeElementAnchorLexemeNode(parser, parent, parentSignal)

            when {
                parser.readText(textIdentifier) -> result.type = RelativeAnchorType.Text
                parser.readText(lineIdentifier) -> result.type = RelativeAnchorType.Line
                else -> {
                    var value = LiteralCommons.parseAnyString(parser, result,
                            RelativeElementAnchorLexemeAnalyzer.signalEndValue)
                    result.type = RelativeAnchorType.StringValue
                    result.value = value

                    if (value == null) {
                        value = LiteralCommons.parseAnyIntervalForLexem(parser, result,
                                RelativeElementAnchorLexemeAnalyzer.signalEndValue)
                        result.type = RelativeAnchorType.IntervalValue
                        result.value = value

                        if (value == null) {
                            value = BitlistNode.parse(parser, result,
                                    RelativeElementAnchorLexemeAnalyzer.signalEndValue)
                            result.type = RelativeAnchorType.BitListValue
                            result.value = value

                            if (value == null) {
                                return null
                            }
                        }
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }

    // ENUMS ------------------------------------------------------------------

    enum class RelativeAnchorType(val identifier: String) {
        Text(textIdentifier),
        Line(lineIdentifier),
        StringValue(""),
        BitListValue(""),
        IntervalValue(""),
    }
}
