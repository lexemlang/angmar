package org.lexem.angmar

import com.google.gson.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.printer.*

/**
 * The resulting nodes of a Lexem analysis.
 */
class LexemMatch internal constructor(analyzer: LexemAnalyzer, node: LxmNode) : JsonSerializable {
    val text = analyzer.text
    val from = node.getFrom(analyzer.memory).primitive
    val to = node.getTo(analyzer.memory)!!.primitive
    val children = node.getChildrenAsList(analyzer.memory).map { position ->
        LexemMatch(analyzer, position.dereference(analyzer.memory) as LxmNode)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the textual content of the node.
     */
    fun getContent(): Any {
        val result = AnalyzerCommons.substringReader(text, from, to)
        return if (result is LxmString) {
            result.primitive
        } else {
            result as LxmBitList
            result.primitive
        }
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("from", from.position().toString())
        result.addProperty("to", to.position().toString())
        result.addProperty("content", getContent().toString())
        result.add("children", SerializationUtils.listToTest(children))

        return result
    }
}
