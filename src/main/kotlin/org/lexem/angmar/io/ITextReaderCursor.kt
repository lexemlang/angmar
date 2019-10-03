package org.lexem.angmar.io

import org.lexem.angmar.errors.*


/**
 * Interface for a generic read cursor.
 */
interface ITextReaderCursor {
    /**
     * Returns the character of the sequence or null if the cursor is at the end of the content.
     */
    fun char(): Char?

    /**
     * Returns the position over the character sequence.
     */
    fun position(): Int

    /**
     * Returns the position as a line-column position.
     */
    fun lineColumn(): Pair<Int, Int>

    /**
     * Restores the reader's internal state with the information of this cursor.
     */
    fun restore()

    /**
     * Gets the reader associated with the cursor.
     */
    fun getReader(): ITextReader

    // STATIC -----------------------------------------------------------------

    /**
     * Used as default value for a pointer.
     */
    object Empty : ITextReaderCursor {
        override fun char(): Char? {
            throw AngmarException("This method is intentionally not implemented.")
        }

        override fun position(): Int {
            throw AngmarException("This method is intentionally not implemented.")
        }

        override fun lineColumn(): Pair<Int, Int> {
            throw AngmarException("This method is intentionally not implemented.")
        }

        override fun restore() {
            throw AngmarException("This method is intentionally not implemented.")
        }

        override fun getReader(): ITextReader {
            throw AngmarException("This method is intentionally not implemented.")
        }
    }
}
