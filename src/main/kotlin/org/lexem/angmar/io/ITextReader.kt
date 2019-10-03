package org.lexem.angmar.io

import org.lexem.angmar.errors.*


/**
 * Interface for a generic text reader.
 */
interface ITextReader {
    /**
     * Returns the source of this reader. Usually a file path.
     */
    fun getSource(): String

    /**
     * Returns the current character of the sequence.
     */
    fun currentChar() = nextCharAt(0)

    /**
     * Returns the current position over the character sequence.
     */
    fun currentPosition(): Int

    /**
     * Returns the character at the specified position or null if there no any character at that position.
     *
     * @param position The position of the character to read.
     */
    fun charAt(position: Int): Char? {
        if (position < 0) {
            throw AngmarIOException("The param 'count' can't be negative")
        }

        val currentPosition = currentPosition()
        return when {
            position < currentPosition -> prevCharAt(currentPosition - position)
            position > currentPosition -> nextCharAt(position - currentPosition)
            else -> currentChar()
        }
    }

    /**
     * Returns the next character at the specified offset from the current position or null if there no more characters.
     *
     * @param offset The position of the character to read from the reader's cursor.
     */
    fun nextCharAt(offset: Int = 1): Char?

    /**
     * Returns the previous character at the specified offset from the current position backwards or null if there no more characters.
     *
     * @param offset The position of the character to read backwards from the reader's cursor.
     */
    fun prevCharAt(offset: Int = 1): Char?

    /**
     * Indicates whether the reader cursor is at the end of the content.
     */
    fun isStart() = currentPosition() == 0

    /**
     * Indicates whether the reader cursor is at the end of the content.
     */
    fun isEnd(): Boolean

    /**
     * Advances the internal reader's cursor 'count' characters forward.
     *
     * @param count The number of characters to advance.
     * @return A boolean value indicating whether the reader has been able to moved its cursor to the specified position.
     */
    fun advance(count: Int = 1): Boolean

    /**
     * Gets back the internal reader's cursor 'count' characters backward.
     *
     * @param count The number of characters to advance.
     * @return A boolean value indicating whether the reader has been able to moved its cursor to the specified position.
     */
    fun back(count: Int = 1): Boolean

    /**
     * Sets the internal reader's cursor to the specified position.
     *
     * @param position The position to go.
     * @return A boolean value indicating whether the reader has been able to moved its cursor to the specified position.
     */
    fun setPosition(position: Int): Boolean {
        if (position < 0) {
            throw AngmarIOException("The param 'count' can't be negative")
        }

        val currentPosition = currentPosition()
        return when {
            position < currentPosition -> back(currentPosition - position)
            position > currentPosition -> advance(position - currentPosition)
            else -> true
        }
    }

    /**
     * Saves the reader's current internal stat, allowing for fast restoring it.
     */
    fun saveCursor(): ITextReaderCursor

    /**
     * Restarts the reader setting its position to the origin.
     */
    fun restart()

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
}
