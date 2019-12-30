package org.lexem.angmar.analyzer.memory.bignode

import org.lexem.angmar.analyzer.data.*

/**
 * The representation of a stack cell.
 */
internal data class BigNodeStackCell(val value: LexemPrimitive, val previousCell: BigNodeStackCell?) {

    /**
     * Whether this cell has a previous or not.
     */
    val hasPrevious = previousCell != null

    // OVERRIDE METHODS -------------------------------------------------------

    override fun toString() = "$value -> hasPrevious: $hasPrevious"
}

