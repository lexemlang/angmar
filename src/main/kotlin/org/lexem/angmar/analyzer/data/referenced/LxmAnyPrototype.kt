package org.lexem.angmar.analyzer.data.referenced

import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.memory.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.config.*

/**
 * The Lexem value of the Any prototype.
 */
internal open class LxmAnyPrototype : LxmObject {

    // CONSTRUCTORS -----------------------------------------------------------

    constructor(memory: LexemMemory) : super(memory)
    constructor(memory: LexemMemory, oldVersion: LxmAnyPrototype, toClone: Boolean) : super(memory, oldVersion, toClone)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(memory: LexemMemory): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(memory, toWrite = false)
        return context.getPropertyValue(memory, ObjectType.TypeName) as LxmReference
    }

    override fun clone(memory: LexemMemory) = LxmAnyPrototype(memory, this,
            toClone = countOldVersions() >= Consts.Memory.maxVersionCountToFullyCopyAValue)

    override fun toString() = "[ANY PROTOTYPE] ${super.toString()}"
}
