package org.lexem.angmar.io

import org.lexem.angmar.errors.*


/**
 * Interface for a generic text reader.
 */
interface ITextReader : IReader {
    /**
     * Returns the current character of the sequence.
     */
    fun currentChar() = nextChar(0)

    /**
     * Returns the character at the specified position or null if there no any character at that position.
     *
     * @param position The position of the character to read.
     */
    fun charAt(position: Int): Char? {
        if (position < 0) {
            throw AngmarIOException("The param 'position' can't be negative")
        }

        val currentPosition = currentPosition()
        return when {
            position < currentPosition -> prevChar(currentPosition - position)
            position > currentPosition -> nextChar(position - currentPosition)
            else -> currentChar()
        }
    }

    /**
     * Returns the next character at the specified offset from the current position or null if there is no more characters.
     *
     * @param offset The position of the character to read from the reader's cursor.
     */
    fun nextChar(offset: Int = 1): Char?

    /**
     * Returns the previous character at the specified offset from the current position backwards or null if there is no more characters.
     *
     * @param offset The position of the character to read backwards from the reader's cursor.
     */
    fun prevChar(offset: Int = 1): Char?

    /**
     * Reads all the content keeping the current cursor position.
     */
    fun readAllText(): String

    /**
     * Returns a subsection of the reader.
     */
    fun substring(from: ITextReaderCursor, to: ITextReaderCursor): String {
        val currentPosition = saveCursor()
        val length = to.position() - from.position()

        from.restore()
        val sb = StringBuilder(length)
        repeat(length) {
            sb.append(currentChar())
            advance()
        }

        currentPosition.restore()
        return sb.toString()
    }

    override fun saveCursor(): ITextReaderCursor
}
