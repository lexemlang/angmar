package org.lexem.angmar.io.readers

import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.utils.*
import java.io.*
import java.util.*

/**
 * Text reader from a [BooleanArray].
 */
class IOBinaryReader internal constructor(private val sourceFile: String, private val content: BitSet,
        private val contentSize: Int) : IBinaryReader {
    private var position = 0

    // OVERRIDE METHODS -------------------------------------------------------

    override fun getSource() = sourceFile

    override fun currentPosition() = position

    override fun nextBit(offset: Int): Boolean? {
        if (offset < 0) {
            throw AngmarIOException("The param 'offset' can't be negative")
        }

        return if (offset + position < contentSize) {
            content.get(offset + position)
        } else {
            null
        }
    }

    override fun prevBit(offset: Int): Boolean? {
        if (offset < 0) {
            throw AngmarIOException("The param 'offset' can't be negative")
        }

        return if (position - offset >= 0) {
            content.get(position - offset)
        } else {
            null
        }
    }

    override fun isEnd() = position >= contentSize

    override fun advance(count: Int): Boolean {
        if (count < 0) {
            throw AngmarIOException("The param 'count' can't be negative")
        }

        if (count + position > contentSize) {
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

    override fun saveCursor() = BinaryReaderCursor(this, currentPosition())

    override fun saveCursorAt(position: Int): BinaryReaderCursor? {
        if (position < 0 || position > getLength()) {
            return null
        }

        return BinaryReaderCursor(this, position)
    }

    /**
     * Restores a [BinaryReaderCursor] generated for this [IOBinaryReader].
     * @param cursor A [BinaryReaderCursor] of this [IOBinaryReader] to restore.
     */
    internal fun restoreCursor(cursor: BinaryReaderCursor) {
        if (cursor.getReader() != this) {
            throw AngmarIOException("A reader can only restore a cursor created for itself")
        }

        position = cursor.position()
    }

    override fun restart() {
        position = 0
    }

    override fun readAllContent() = Pair(contentSize, content.clone() as BitSet)

    override fun getLength() = contentSize

    override fun subSet(from: IBinaryReaderCursor, to: IBinaryReaderCursor) =
            content.get(from.position(), to.position())

    // STATIC -----------------------------------------------------------------

    companion object {
        /**
         * Creates a new [IOBinaryReader] with the specified content.
         */
        fun from(content: ByteArray) = IOBinaryReader("", BitSet.valueOf(content.map {
            it.reverseBits()
        }.toByteArray()), content.size * 8)

        /**
         * Creates a new [IOBinaryReader] with the content of a file.
         */
        fun from(file: File): IOBinaryReader {
            val bytes = file.readBytes()
            return IOBinaryReader(file.canonicalPath, BitSet.valueOf(bytes), bytes.size * 8)
        }
    }

    /**
     * Cursor for [IOBinaryReader]
     */
    class BinaryReaderCursor constructor(private val reader: IOBinaryReader, at: Int) : IBinaryReaderCursor {
        private val position = at
        private val bitValue = reader.content[at]

        override fun position() = position
        override fun bit() = bitValue

        override fun restore() {
            reader.restoreCursor(this)
        }

        override fun getReader() = reader

        override fun toString() = "(position: $position, bit: $bitValue)"
    }
}
