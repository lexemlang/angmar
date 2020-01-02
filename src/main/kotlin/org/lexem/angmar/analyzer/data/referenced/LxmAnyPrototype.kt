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

    constructor(memory: IMemory) : super(memory)
    private constructor(memory: IMemory, oldVersion: LxmAnyPrototype) : super(memory, oldVersion)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: IMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, ObjectType.TypeName) as LxmReference
    }

    override fun memoryClone(memory: IMemory) = LxmAnyPrototype(memory, this)

    override fun toString() = "[Any Prototype] ${super.toString()}"
}
