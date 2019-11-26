package org.lexem.angmar.io

import org.lexem.angmar.errors.*


/**
 * Interface for a generic [IBinaryReader] cursor.
 */
interface IBinaryReaderCursor : IReaderCursor {
    /**
     * Returns the bit of the sequence or null if the cursor is at the end of the content.
     */
    fun bit(): Boolean?

    // STATIC -----------------------------------------------------------------

    /**
     * Used as default value for a pointer.
     */
    object Empty : IBinaryReaderCursor {
        override fun bit(): Boolean? {
            throw AngmarUnreachableException()
        }

        override fun position(): Int {
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
