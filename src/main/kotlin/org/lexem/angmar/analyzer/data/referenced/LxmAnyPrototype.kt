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
    private constructor(memory: LexemMemory, oldVersion: LxmAnyPrototype) : super(memory, oldVersion, true)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, ObjectType.TypeName) as LxmReference
    }

    override fun memoryShift(memory: LexemMemory) = LxmAnyPrototype(memory, oldVersion = this)

    override fun toString() = "[Any Prototype] ${super.toString()}"
}
