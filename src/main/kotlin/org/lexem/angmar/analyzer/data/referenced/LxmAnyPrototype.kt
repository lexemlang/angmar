package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*

/**
 * The Lexem value of the Any prototype.
 */
internal open class LxmAnyPrototype : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory)
    constructor(memory: LexemMemory, oldContext: LxmAnyPrototype) : super(memory, oldContext)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getPrototype(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getCurrentContext(memory, toWrite = false)
        val type =
                context.getPropertyValue(memory, AnyType.TypeName)!!.dereference(memory, toWrite = false) as LxmObject

        return type.getPropertyValue(memory, AnalyzerCommons.Identifiers.Prototype) as LxmReference
    }


    override fun clone(memory: LexemMemory) = LxmAnyPrototype(memory, this)

    override fun toString() = "[ANY PROTOTYPE] ${super.toString()}"
}
