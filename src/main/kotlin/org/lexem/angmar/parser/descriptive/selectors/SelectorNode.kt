package org.lexem.angmar.parser.descriptive.selectors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.*
import org.lexem.angmar.analyzer.nodes.descriptive.selectors.SelectorAnalyzer.signalEndFirstProperty
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for selectors.
 */
internal class SelectorNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var isAddition = false
    var name: NameSelectorNode? = null
    val properties = mutableListOf<PropertySelectorNode>()
    val methods = mutableListOf<MethodSelectorNode>()

    override fun toString() = StringBuilder().apply {
        (listOf(name) + properties + methods).joinToString(" ")
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isAddition", isAddition)
        result.add("name", name?.toTree())
        result.add("properties", SerializationUtils.listToTest(properties))
        result.add("methods", SerializationUtils.listToTest(methods))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = SelectorAnalyzer.stateMachine(analyzer, signal, this)

    companion object {

        /**
         * Parses a selector.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): SelectorNode? {
            parser.fromBuffer(parser.reader.currentPosition(), SelectorNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = SelectorNode(parser, parent, parentSignal)

            var prev = false

            // Name
            result.name = NameSelectorNode.parse(parser, result, SelectorAnalyzer.signalEndName)
            if (result.name != null) {
                prev = true
            }

            // Properties
            while (true) {
                val initIterationCursor = parser.reader.saveCursor()

                if (prev) {
                    WhitespaceNode.parse(parser)
                }

                val property =
                        PropertySelectorNode.parse(parser, result, result.properties.size + signalEndFirstProperty)

                if (property == null) {
                    initIterationCursor.restore()
                    break
                }

                result.properties.add(property)

                prev = true
            }

            // Methods
            while (true) {
                val initIterationCursor = parser.reader.saveCursor()

                if (prev) {
                    WhitespaceNode.parse(parser)
                }

                val method = MethodSelectorNode.parse(parser, result,
                        result.properties.size + result.methods.size + signalEndFirstProperty)

                if (method == null) {
                    initIterationCursor.restore()
                    break
                }

                result.methods.add(method)

                prev = true
            }

            if (result.name == null && result.properties.isEmpty() && result.methods.isEmpty()) {
                return null
            }

            return parser.finalizeNode(result, initCursor)
        }

        /**
         * Parses a selector for an Addition.
         */
        fun parseForAddition(parser: LexemParser, parent: ParserNode, parentSignal: Int): SelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = SelectorNode(parser, parent, parentSignal)
            result.isAddition = true

            // Name
            result.name =
                    NameSelectorNode.parseForAddition(parser, result, SelectorAnalyzer.signalEndName) ?: return null

            // Properties
            while (true) {
                val initIterationCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val property = PropertySelectorNode.parseForAddition(parser, result,
                        result.properties.size + signalEndFirstProperty)

                if (property == null) {
                    initIterationCursor.restore()
                    break
                }

                result.properties.add(property)
            }

            return parser.finalizeNodeNoBuffer(result, initCursor)
        }
    }
}
