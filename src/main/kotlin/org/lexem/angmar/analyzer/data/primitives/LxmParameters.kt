package org.lexem.angmar.analyzer.data.primitives

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.errors.*

/**
 * The Lexem value of the parameters of a function.
 */
internal class LxmParameters : LexemPrimitive {
    private val parameters = mutableListOf<String>()
    var positionalSpread: String? = null
        private set
    var namedSpread: String? = null
        private set

    // METHODS ----------------------------------------------------------------

    /**
     * Adds a parameter.
     */
    fun addParameter(name: String) {
        parameters += name
    }

    /**
     * Gets the parameter list.
     */
    fun getParameters() = parameters.toList()

    /**
     * Sets the positional spread argument name.
     */
    fun setPositionalSpreadArgument(name: String) {
        positionalSpread = name
    }

    /**
     * Sets the named spread argument name.
     */
    fun setNamedSpreadArgument(name: String) {
        namedSpread = name
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getHashCode(memory: LexemMemory) = throw AngmarUnreachableException()

    override fun toString() =
            "[Parameters] (parameters: $parameters, positionalSpread: $positionalSpread, namedSpread: $namedSpread)"
}
