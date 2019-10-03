package org.lexem.angmar

import com.google.gson.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.io.printer.*

/**
 * The resulting nodes of a Lexem analysis.
 */
class LexemMatch internal constructor(analyzer: LexemAnalyzer, node: LxmNode) : ITreeLikePrintable {
    val text = analyzer.text
    val from = node.from
    val to = node.to!!
    val children = node.getChildrenAsList(analyzer.memory).map { position ->
        LexemMatch(analyzer, position.dereference(analyzer.memory) as LxmNode)
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Gets the textual content of the node.
     */
    fun getContent() = text.substring(from, to)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("from", from.position().toString())
        result.addProperty("to", to.position().toString())
        result.addProperty("content", getContent())
        result.add("children", TreeLikePrintable.listToTest(children))

        return result
    }
}
