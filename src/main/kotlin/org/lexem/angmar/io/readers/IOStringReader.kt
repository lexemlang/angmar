package org.lexem.angmar.io.readers

import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import java.io.*

/**
 * Text reader from a [String].
 */
class IOStringReader internal constructor(private val sourceFile: String, private val text: String) : ITextReader {
    private var position = 0

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getSource() = sourceFile

    override fun currentPosition() = position

    override fun nextChar(offset: Int): Char? {
        if (offset < 0) {
            throw AngmarIOException("The param 'offset' can't be negative")
        }

        return text.getOrNull(offset + position)
    }

    override fun prevChar(offset: Int): Char? {
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

    override fun saveCursor() = StringReaderCursor(this, currentPosition())

    override fun saveCursorAt(position: Int): StringReaderCursor? {
        if (position < 0 || position > getLength()) {
            return null
        }

        return StringReaderCursor(this, position)
    }

    /**
     * Restores a [StringReaderCursor] generated for this [IOStringReader].
     * @param cursor A [StringReaderCursor] of this [IOStringReader] to restore.
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

    override fun getLength() = text.length

    override fun substring(from: ITextReaderCursor, to: ITextReaderCursor) =
            text.substring(from.position(), to.position())

    // STATIC -----------------------------------------------------------------

    companion object {
        /**
         * Creates a new [IOStringReader] with the specified content and source.
         */
        fun from(text: String, source: String = "") = IOStringReader(source, text)

        /**
         * Creates a new [IOStringReader] with the content of a file.
         */
        fun from(file: File) = IOStringReader(file.canonicalPath, file.readText(Charsets.UTF_8))
    }

    /**
     * Cursor for [IOStringReader]
     */
    class StringReaderCursor internal constructor(private val reader: IOStringReader, at: Int) : ITextReaderCursor {
        private val position = at
        private val character = reader.text.getOrNull(at)

        override fun position() = position
        override fun char() = character

        override fun restore() {
            reader.restoreCursor(this)
        }

        override fun lineColumn(): Pair<Int, Int> {
            var line = 1
            var column = 1
            var lastWasCR = false

            for ((index, element) in reader.text.withIndex()) {
                if (index >= position) {
                    break
                }

                when (element) {
                    '\n' -> {
                        if (lastWasCR) {
                            line -= 1
                        }

                        line += 1
                        column = 1
                        lastWasCR = false
                    }
                    '\r' -> {
                        line += 1
                        column = 1
                        lastWasCR = true
                    }
                    else -> {
                        column += 1
                        lastWasCR = false
                    }
                }
            }

            return Pair(line, column)
        }

        override fun getReader() = reader

        override fun toString() = "(position: $position, char: $character)"
    }
}
