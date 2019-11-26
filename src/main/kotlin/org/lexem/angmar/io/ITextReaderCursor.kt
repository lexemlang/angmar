package org.lexem.angmar.io

import org.lexem.angmar.errors.*


/**
 * Interface for a generic [ITextReader] cursor.
 */
interface ITextReaderCursor : IReaderCursor {
    /**
     * Returns the character of the sequence or null if the cursor is at the end of the content.
     */
    fun char(): Char?

    /**
     * Returns the position as a line-column position.
     */
    fun lineColumn(): Pair<Int, Int>

    // STATIC -----------------------------------------------------------------

    /**
     * Used as default value for a pointer.
     */
    object Empty : ITextReaderCursor {
        override fun char(): Char? {
            throw AngmarUnreachableException()
        }

        override fun position(): Int {
            throw AngmarUnreachableException()
        }

        override fun lineColumn(): Pair<Int, Int> {
            throw AngmarUnreachableException()
        }

        override fun restore() {
            throw AngmarUnreachableException()
        }

        override fun getReader(): ITextReader {
            throw AngmarUnreachableException()
        }
    }
}
