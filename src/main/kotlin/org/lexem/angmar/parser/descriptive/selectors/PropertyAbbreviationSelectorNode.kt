package org.lexem.angmar.parser.descriptive.selectors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for property abbreviations of selectors.
 */
internal class PropertyAbbreviationSelectorNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    var isNegated = false
    var isAddition = false
    var isAtIdentifier = false
    var propertyBlock: PropertyBlockSelectorNode? = null
    lateinit var name: IdentifierNode

    override fun toString() = StringBuilder().apply {
        if (isNegated) {
            append(notOperator)
        }

        if (isAtIdentifier) {
            append(atPrefix)
        }

        append(name)

        if (propertyBlock != null) {
            append(propertyBlock)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isNegated", isNegated)
        result.addProperty("isAddition", isAddition)
        result.addProperty("isAtIdentifier", isAtIdentifier)
        result.add("name", name.toTree())
        result.add("propertyBlock", propertyBlock?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PropertyAbbreviationSelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val notOperator = PrefixOperatorNode.notOperator
        const val atPrefix = "@"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a property abbreviation of a selector.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): PropertyAbbreviationSelectorNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PropertyAbbreviationSelectorNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = PropertyAbbreviationSelectorNode(parser, parent, parentSignal)

            if (parser.readText(notOperator)) {
                result.isNegated = true

                result.isAtIdentifier = parser.readText(atPrefix)

                val name = IdentifierNode.parse(parser, result, PropertyAbbreviationSelectorAnalyzer.signalEndName)
                if (name == null) {
                    initCursor.restore()
                    return null
                }

                result.name = name
            } else {
                result.isAtIdentifier = parser.readText(atPrefix)

                val name = IdentifierNode.parse(parser, result, PropertyAbbreviationSelectorAnalyzer.signalEndName)
                if (name == null) {
                    initCursor.restore()
                    return null
                }

                result.name = name

                result.propertyBlock = PropertyBlockSelectorNode.parse(parser, result,
                        PropertyAbbreviationSelectorAnalyzer.signalEndPropertyBlock)
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a property abbreviation of a selector for an addition.
         */
        fun parseForAddition(parser: LexemParser, parent: ParserNode,
                parentSignal: Int): PropertyAbbreviationSelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = PropertyAbbreviationSelectorNode(parser, parent, parentSignal)
            result.isAddition = true

            if (parser.readText(notOperator)) {
                result.isNegated = true

                val name = IdentifierNode.parse(parser, result, PropertyAbbreviationSelectorAnalyzer.signalEndName)
                if (name == null) {
                    initCursor.restore()
                    return null
                }

                result.name = name
            } else {
                val name = IdentifierNode.parse(parser, result, PropertyAbbreviationSelectorAnalyzer.signalEndName)
                if (name == null) {
                    initCursor.restore()
                    return null
                }

                result.name = name

                result.propertyBlock = PropertyBlockSelectorNode.parseForAddition(parser, result,
                        PropertyAbbreviationSelectorAnalyzer.signalEndPropertyBlock)
            }

            return parser.finalizeNodeNoBuffer(result, initCursor)
        }

    }
}
