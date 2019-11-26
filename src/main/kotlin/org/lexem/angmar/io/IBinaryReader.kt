package org.lexem.angmar.io

import org.lexem.angmar.errors.*
import java.util.*


/**
 * Interface for a generic binary reader.
 */
interface IBinaryReader : IReader {
    /**
     * Returns the current bit of the sequence.
     */
    fun currentBit() = nextBit(0)

    /**
     * Returns the bit at the specified position or null if there no any bit at that position.
     *
     * @param position The position of the bit to read.
     */
    fun bitAt(position: Int): Boolean? {
        if (position < 0) {
            throw AngmarIOException("The param 'position' can't be negative")
        }

        val currentPosition = currentPosition()
        return when {
            position < currentPosition -> prevBit(currentPosition - position)
            position > currentPosition -> nextBit(position - currentPosition)
            else -> currentBit()
        }
    }

    /**
     * Returns the next bit at the specified offset from the current position or null if there is no more bits.
     *
     * @param offset The position of the bit to read from the reader's cursor.
     */
    fun nextBit(offset: Int = 1): Boolean?

    /**
     * Returns the previous bit at the specified offset from the current position backwards or null if there is no more bits.
     *
     * @param offset The position of the bit to read backwards from the reader's cursor.
     */
    fun prevBit(offset: Int = 1): Boolean?

    /**
     * Reads all the content keeping the current cursor position.
     */
    fun readAllContent(): Pair<Int, BitSet>

    /**
     * Returns a subsection of the reader.
     */
    fun subSet(from: IBinaryReaderCursor, to: IBinaryReaderCursor): BitSet {
        val currentPosition = saveCursor()
        val length = to.position() - from.position()

        from.restore()
        val result = BitSet(length)
        repeat(length) {
            if (currentBit()!!) {
                result.set(it)
            }
            advance()
        }

        currentPosition.restore()
        return result
    }

    override fun saveCursor(): IBinaryReaderCursor
}
