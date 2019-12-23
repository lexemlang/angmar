package org.lexem.angmar.parser.descriptive.selectors

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.selectors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for selectors.
 */
internal class SelectorNode private constructor(parser: LexemParser, parent: ParserNode) : ParserNode(parser, parent) {
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

    override fun compile(parent: CompiledNode, parentSignal: Int) = SelectorCompiled.compile(parent, parentSignal, this)

    companion object {

        /**
         * Parses a selector.
         */
        fun parse(parser: LexemParser, parent: ParserNode): SelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = SelectorNode(parser, parent)

            var prev = false

            // Name
            result.name = NameSelectorNode.parse(parser, result)
            if (result.name != null) {
                prev = true
            }

            // Properties
            while (true) {
                val initIterationCursor = parser.reader.saveCursor()

                if (prev) {
                    WhitespaceNode.parse(parser)
                }

                val property = PropertySelectorNode.parse(parser, result)

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

                val method = MethodSelectorNode.parse(parser, result)

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
        fun parseForAddition(parser: LexemParser, parent: ParserNode): SelectorNode? {
            val initCursor = parser.reader.saveCursor()
            val result = SelectorNode(parser, parent)
            result.isAddition = true

            // Name
            result.name = NameSelectorNode.parseForAddition(parser, result) ?: return null

            // Properties
            while (true) {
                val initIterationCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val property = PropertySelectorNode.parseForAddition(parser, result)

                if (property == null) {
                    initIterationCursor.restore()
                    break
                }

                result.properties.add(property)
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
