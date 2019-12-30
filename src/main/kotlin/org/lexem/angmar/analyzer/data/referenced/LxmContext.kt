package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The Lexem value for the context.
 */
internal class LxmContext : LxmObject {
    val type: LxmContextType

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory, type: LxmContextType) : super(memory) {
        this.type = type
    }

    constructor(memory: LexemMemory, higherContextReference: LxmContext, type: LxmContextType) : super(memory,
            higherContextReference) {
        this.type = type
    }

    private constructor(bigNode: BigNode, version: LxmContext) : super(bigNode, version) {
        type = version.type
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getPrototype(memory: LexemMemory) = prototypeReference ?: let {
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        val type =
                context.getPropertyValue(memory, AnyType.TypeName)!!.dereference(memory, toWrite = false) as LxmObject

        return type.getPropertyValue(memory, AnalyzerCommons.Identifiers.Prototype) as LxmReference
    }

    override fun memoryClone(bigNode: BigNode) = LxmContext(bigNode, this)

    override fun toString() = "[Context] ${super.toString()}"

    // STATIC -----------------------------------------------------------------

    /**
     * The types of contexts.
     */
    enum class LxmContextType {
        StdLib,
        Root,
        Function,
        Expression,
        Filter;

        /**
         * Checks whether this type is functional or not.
         */
        fun isFunctional() = when (this) {
            StdLib -> true
            Root -> true
            Function -> true
            else -> false
        }
    }
}
