package org.lexem.angmar.io.readers

import org.lexem.angmar.errors.AngmarIOException
import org.lexem.angmar.io.ITextReader
import org.lexem.angmar.io.ITextReaderCursor
import java.io.File

/**
 * Text reader from a [String].
 */
class CustomStringReader private constructor(private val sourceFile: String, private val text: String) :
    ITextReader {
    private var position = 0

    override fun getSource() = sourceFile

    override fun currentPosition() = position

    override fun nextCharAt(offset: Int): Char? {
        if (offset < 0) {
            throw AngmarIOException("The param 'offset' can't be negative")
        }

        return text.getOrNull(offset + position)
    }

    override fun prevCharAt(offset: Int): Char? {
        if (offset < 0) {
            throw AngmarIOException("The param 'offset' can't be negative")
        }

        return text.getOrNull(position - offset)
    }

    override fun isEnd() = position >= text.length

    override fun advance(count: Int): Boolean {
        if (count < 0) {
            throw AngmarIOException("The param 'count' can't be negative")
        }

        if (count + position > text.length) {
            return false
        }

        position += count
        return true
    }

    override fun back(count: Int): Boolean {
        if (count < 0) {
            throw AngmarIOException("The param 'count' can't be negative")
        }

        if (position - count < 0) {
            return false
        }

        position -= count
        return true
    }

    override fun saveCursor(): ITextReaderCursor =
        StringReaderCursor(this)

    /**
     * Restores a [StringReaderCursor] generated for this [CustomStringReader].
     * @param cursor A [StringReaderCursor] of this [CustomStringReader] to restore.
     */
    internal fun restoreCursor(cursor: StringReaderCursor) {
        if (cursor.getReader() != this) {
            throw AngmarIOException("A reader can only restore a cursor created for itself")
        }

        position = cursor.position()
    }


    override fun restart() {
        position = 0
    }

    override fun readAllText() = text

    companion object {
        /**
         * Creates a new [CustomStringReader] with the specified content.
         */
        fun from(text: String): CustomStringReader =
            CustomStringReader("<internal>", text)

        /**
         * Creates a new [CustomStringReader] with the content of a file.
         */
        fun from(file: File): CustomStringReader =
            CustomStringReader(file.canonicalPath, file.readText(Charsets.UTF_8))
    }
}

/**
 * Cursor for [CustomStringReader]
 */
class StringReaderCursor constructor(private val reader: CustomStringReader) :
    ITextReaderCursor {
    private val position = reader.currentPosition()
    private val character = reader.currentChar()

    override fun position() = position
    override fun char() = character

    override fun restore() {
        reader.restoreCursor(this)
    }

    override fun getReader() = reader
}
