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
    private constructor(bigNode: BigNode, oldVersion: LxmAnyPrototype) : super(bigNode, oldVersion)

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getType(bigNode: BigNode): LxmReference {
        val context = AnalyzerCommons.getStdLibContext(bigNode, toWrite = false)
        return context.getPropertyValue(ObjectType.TypeName) as LxmReference
    }

    override fun memoryClone(bigNode: BigNode) = LxmAnyPrototype(bigNode, this)

    override fun toString() = "[Any Prototype] ${super.toString()}"
}
