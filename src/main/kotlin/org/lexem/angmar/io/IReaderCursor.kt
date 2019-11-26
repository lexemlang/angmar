package org.lexem.angmar.io

import org.lexem.angmar.errors.*


/**
 * Interface for a generic [IReader] cursor.
 */
interface IReaderCursor {
    /**
     * Returns the position over the character sequence.
     */
    fun position(): Int

    /**
     * Restores the reader's internal state with the information of this cursor.
     */
    fun restore()

    /**
     * Gets the reader associated with the cursor.
     */
    fun getReader(): IReader

    // STATIC -----------------------------------------------------------------

    /**
     * Used as default value for a pointer.
     */
    object Empty : IReaderCursor {
        override fun position(): Int {
            throw AngmarUnreachableException()
        }

        override fun restore() {
            throw AngmarUnreachableException()
        }

        override fun getReader(): IReader {
            throw AngmarUnreachableException()
        }
    }
}
