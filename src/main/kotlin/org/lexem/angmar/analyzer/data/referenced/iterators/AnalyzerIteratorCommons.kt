package org.lexem.angmar.analyzer.data.referenced.iterators

import org.lexem.angmar.analyzer.data.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.data.referenced.*
import org.lexem.angmar.analyzer.memory.*

/**
 * Generic commons for the iterators.
 */
internal object AnalyzerIteratorCommons {
    /**
     * Creates an iterator from an object.
     */
    fun createIterator(memory: LexemMemory, value: LexemPrimitive): LexemIterator? =
            when (val derefValue = value.dereference(memory)) {
                is LxmString -> LxmStringIterator(memory, derefValue.primitive)
                is LxmInterval -> LxmIntervalIterator(memory, derefValue.primitive)
                is LxmList -> LxmListIterator(memory, value as LxmReference)
                is LxmSet -> LxmSetIterator(memory, value as LxmReference)
                is LxmObject -> LxmObjectIterator(memory, value as LxmReference)
                is LxmMap -> LxmMapIterator(memory, value as LxmReference)
                else -> null
            }
}
