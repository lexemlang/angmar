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

    constructor(memory: IMemory, type: LxmContextType) : super(memory) {
        this.type = type
    }

    constructor(memory: IMemory, higherContextReference: LxmContext, type: LxmContextType) : super(memory,
            prototype = higherContextReference) {
        this.type = type
    }

    private constructor(memory: IMemory, version: LxmContext) : super(memory, version) {
        type = version.type
    }

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getPrototype(memory: IMemory) = prototypeReference ?: let {
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        val type =
                context.getPropertyValue(memory, AnyType.TypeName)!!.dereference(memory, toWrite = false) as LxmObject

        return type.getPropertyValue(memory, AnalyzerCommons.Identifiers.Prototype) as LxmReference
    }

    override fun memoryClone(memory: IMemory) = LxmContext(memory, this)

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
