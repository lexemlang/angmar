package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * The Lexem value of a destructuring.
 */
internal class LxmDestructuring : LexemPrimitive {
    var alias: String? = null
        private set
    private val elements = mutableListOf<Property>()
    var spread: Property? = null
        private set

    // METHODS ----------------------------------------------------------------

    /**
     * Adds an element to the destructuring.
     */
    fun setAlias(alias: String) {
        this.alias = alias
    }

    /**
     * Adds an element to the destructuring.
     */
    fun addElement(original: String, isConstant: Boolean) = addElement(original, original, isConstant)

    /**
     * Adds an element to the destructuring with an alias.
     */
    fun addElement(original: String, alias: String, isConstant: Boolean) {
        elements += Property(original, alias, isConstant)
    }

    /**
     * Gets the elements of the destructuring.
     */
    fun getElements() = elements.toList()

    /**
     * Sets the spread element of the destructuring.
     */
    fun setSpread(alias: String, isConstant: Boolean) {
        spread = Property(alias, alias, isConstant)
    }

    /**
     * Destructures an object adding variables in the 'to' object.
     */
    fun destructureObject(memory: LexemMemory, value: LxmObject, to: LxmObject,
            forceConstant: Boolean = false): MutableSet<String> {
        val setVars = mutableSetOf<String>()

        // Add global alias.
        if (alias != null) {
            to.setProperty(memory, alias!!, value, isConstant = forceConstant)
            setVars.add(alias!!)
        }

        val elementsAsMap = mutableMapOf<String, Property>()
        elements.forEach {
            elementsAsMap[it.original] = it
        }

        val spreadObject = LxmObject(memory)

        for (property in value.getAllIterableProperties()) {
            if (property.key in elementsAsMap) {
                val element = elementsAsMap[property.key]!!
                to.setProperty(memory, element.alias, property.value.value,
                        isConstant = element.isConstant || forceConstant)
                setVars.add(element.alias)
            } else {
                spreadObject.setProperty(memory, property.key, property.value.value)
            }
        }

        if (spread != null) {
            to.setProperty(memory, spread!!.alias, spreadObject, isConstant = spread!!.isConstant || forceConstant)
            setVars.add(spread!!.alias)
        }

        return setVars
    }

    /**
     * Destructures a list adding variables in the 'to' object.
     */
    fun destructureList(memory: LexemMemory, value: LxmList, to: LxmObject,
            forceConstant: Boolean = false): MutableSet<String> {
        val setVars = mutableSetOf<String>()

        // Add global alias.
        if (alias != null) {
            to.setProperty(memory, alias!!, value, isConstant = forceConstant)
            setVars.add(alias!!)
        }

        val spreadList = LxmList(memory)

        var index = 0
        for (cell in value.getAllCells()) {
            if (index < elements.size) {
                val element = elements[index]
                to.setProperty(memory, element.alias, cell, isConstant = element.isConstant || forceConstant)
                setVars.add(element.alias)
            } else {
                spreadList.addCell(memory, cell)
            }

            index += 1
        }

        if (spread != null) {
            to.setProperty(memory, spread!!.alias, spreadList, isConstant = spread!!.isConstant || forceConstant)
            setVars.add(spread!!.alias)
        }

        return setVars
    }

    /**
     * Creates a new [LxmDestructuring] with the same values of the current one.
     */
    fun clone(): LxmDestructuring {
        val result = LxmDestructuring()
        result.alias = alias
        result.elements.addAll(elements)
        result.spread = spread

        return result
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() = "[Destructuring] (alias: $alias, elements: $elements, spread: $spread)"

    // STATIC -----------------------------------------------------------------

    data class Property(val original: String, val alias: String, val isConstant: Boolean)
}
