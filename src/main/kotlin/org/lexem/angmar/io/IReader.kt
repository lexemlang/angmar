package org.lexem.angmar.io

import org.lexem.angmar.errors.*


/**
 * Interface for a generic reader.
 */
interface IReader {
    /**
     * Returns the source of this reader. Usually a file path.
     */
    fun getSource(): String

    /**
     * Returns the current position over the reader's content.
     */
    fun currentPosition(): Int

    /**
     * Indicates whether the reader cursor is at the end of the content.
     */
    fun isStart() = currentPosition() == 0

    /**
     * Indicates whether the reader cursor is at the end of the content.
     */
    fun isEnd() = currentPosition() == getLength()

    /**
     * Advances the internal reader's cursor 'count' positions forward.
     *
     * @param count The number of positions to advance.
     * @return A boolean value indicating whether the reader has been able to moved its cursor to the specified position.
     */
    fun advance(count: Int = 1): Boolean

    /**
     * Gets back the internal reader's cursor 'count' positions backward.
     *
     * @param count The number of positions to advance.
     * @return A boolean value indicating whether the reader has been able to moved its cursor to the specified position.
     */
    fun back(count: Int = 1): Boolean

    /**
     * Sets the internal reader's cursor to the specified position.
     *
     * @param position The position to go to.
     * @return A boolean value indicating whether the reader has been able to moved its cursor to the specified position.
     */
    fun setPosition(position: Int): Boolean {
        if (position < 0) {
            throw AngmarIOException("The param 'position' can't be negative")
        }

        val currentPosition = currentPosition()
        return when {
            position < currentPosition -> back(currentPosition - position)
            position > currentPosition -> advance(position - currentPosition)
            else -> true
        }
    }

    /**
     * Saves the reader's current internal status allowing a fast restoring.
     */
    fun saveCursor(): IReaderCursor

    /**
     * Saves the reader's status to recover at the specified position.
     */
    fun saveCursorAt(position: Int): IReaderCursor?

    /**
     * Restarts the reader setting its position to the origin.
     */
    fun restart()

    /**
     * Returns the length of the reader.
     */
    fun getLength(): Int
}
