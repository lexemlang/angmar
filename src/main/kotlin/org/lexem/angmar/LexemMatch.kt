package org.lexem.angmar

import com.google.gson.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.data.*
import org.lexem.angmar.io.printer.*

/**
 * The resulting nodes of a Lexem analysis.
 */
class LexemMatch internal constructor(analyzer: LexemAnalyzer, node: LxmNode, removeDefaultProperties: Boolean) :
        JsonSerializable {
    val text = analyzer.text
    val name = node.name
    val from = node.getFrom(analyzer.memory).primitive
    val to = node.getTo(analyzer.memory)!!.primitive
    val children = node.getChildrenAsList(analyzer.memory).map { position ->
        LexemMatch(analyzer, position.dereference(analyzer.memory, toWrite = false) as LxmNode, removeDefaultProperties)
    }.toList()
    val properties: Map<String, Any>

    init {
        val result = hashMapOf<String, Any>()
        val defaultProperties = AnalyzerCommons.getDefaultPropertiesByType(node.type)
        node.getProperties(analyzer.memory, toWrite = false).getAllIterableProperties().forEach { (key, property) ->
            if (removeDefaultProperties && key in defaultProperties && defaultProperties[key] == property.value) {
                return@forEach
            }

            val res: Any? = when (val value = property.value) {
                is LxmLogic -> value.primitive
                is LxmInteger -> value.primitive
                is LxmFloat -> value.primitive
                is LxmString -> value.primitive
                is LxmBitList -> value.primitive
                is LxmInterval -> value.primitive
                else -> null
            }

            if (res != null) {
                result[key] = res
            }
        }

        properties = result
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

    /**
     * Map the properties to a a tree.
     */
    private fun propertiesToTree(): JsonObject {
        val result = JsonObject()

        for ((key, value) in properties) {
            val property = JsonObject()
            result.add(key, property)

            when (value) {
                is Boolean -> {
                    property.addProperty("type", LogicType.TypeName)
                    property.addProperty("value", value)
                }
                is Int -> {
                    property.addProperty("type", IntegerType.TypeName)
                    property.addProperty("value", value)
                }
                is Float -> {
                    property.addProperty("type", FloatType.TypeName)
                    property.addProperty("value", value)
                }
                is String -> {
                    property.addProperty("type", StringType.TypeName)
                    property.addProperty("value", value)
                }
                is BitList -> {
                    property.addProperty("type", BitListType.TypeName)
                    property.addProperty("value", value.toString())
                }
                is IntegerInterval -> {
                    property.addProperty("type", IntervalType.TypeName)
                    property.addProperty("value", value.toHexString())
                }
            }
        }

        return result
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toTree(): JsonObject {
        val result = JsonObject()

        result.addProperty("name", name)
        result.addProperty("from", from.position().toString())
        result.addProperty("to", to.position().toString())

        val properties = propertiesToTree()
        if (properties.size() != 0) {
            result.add("properties", properties)
        }

        val list = SerializationUtils.listToTest(children)
        if (list.size() != 0) {
            result.add("children", list)
        }

        return result
    }
}
