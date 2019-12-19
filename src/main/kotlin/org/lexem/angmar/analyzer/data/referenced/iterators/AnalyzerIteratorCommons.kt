package org.lexem.angmar.analyzer.data.referenced.iterators

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
    fun createIterator(memory: LexemMemory, value: LexemMemoryValue) = when (value) {
        is LxmString -> LxmStringIterator(memory, value.primitive)
        is LxmInterval -> LxmIntervalIterator(memory, value.primitive)
        is LxmList -> LxmListIterator(memory, value)
        is LxmSet -> LxmSetIterator(memory, value)
        is LxmObject -> LxmObjectIterator(memory, value)
        is LxmMap -> LxmMapIterator(memory, value)
        else -> null
    }
}
