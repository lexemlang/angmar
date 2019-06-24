package org.lexem.angmar

import org.lexem.angmar.config.AngmarConfig
import org.lexem.angmar.errors.AngmarIOException
import org.lexem.angmar.io.ITextReader
import org.lexem.angmar.io.ITextReaderCursor
import org.lexem.angmar.io.readers.CustomStringReader
import org.lexem.angmar.nodes.ForwardBuffer
import org.lexem.angmar.nodes.NodeType
import org.lexem.angmar.nodes.ParserNode
import org.lexem.angmar.nodes.literals.NumberNode
import java.io.*

/**
 * Lexer and parser for the Lexem language.
 */
class LexemParser internal constructor(val reader: ITextReader, val config: AngmarConfig = AngmarConfig()) {
    private val forwardBuffer = if (config.forwardBuffer) {
        ForwardBuffer()
    } else {
        null
    }

    // METHODS ----------------------------------------------------------------

    /**
     * Reads any of the chars inside the specified [String].
     * This function does not consume any character.
     */
    internal fun checkAnyChar(text: String): Char? {
        val rChar = reader.currentChar() ?: return null
        if (text.contains(rChar)) {
            return rChar
        }

        return null
    }

    /**
     * Reads any of the chars inside the specified [String].
     */
    internal fun readAnyChar(text: String): Char? {
        val rChar = checkAnyChar(text) ?: return null
        reader.advance()
        return rChar
    }

    /**
     * Reads any char not inside the specified [String].
     * This function does not consume any character.
     */
    internal fun checkNegativeAnyChar(text: String): Char? {
        val rChar = reader.currentChar() ?: return null
        if (!text.contains(rChar)) {
            return rChar
        }

        return null
    }

    /**
     * Reads any char not inside the specified [String].
     */
    internal fun readNegativeAnyChar(text: String): Char? {
        val rChar = checkNegativeAnyChar(text) ?: return null
        reader.advance()
        return rChar
    }

    /**
     * Reads any of the chars inside the specified [Set].
     * This function does not consume any character.
     */
    internal fun checkAnyChar(chars: Set<Char>): Char? {
        val rChar = reader.currentChar()
        if (rChar != null && rChar in chars) {
            return rChar
        }

        return null
    }

    /**
     * Reads any of the chars inside the specified [Set].
     */
    internal fun readAnyChar(chars: Set<Char>): Char? {
        val rChar = checkAnyChar(chars) ?: return null
        reader.advance()
        return rChar
    }

    /**
     * Reads any char not inside the specified [Set].
     * This function does not consume any character.
     */
    internal fun checkNegativeAnyChar(chars: Set<Char>): Char? {
        val rChar = reader.currentChar()
        if (rChar != null && rChar !in chars) {
            return rChar
        }

        return null
    }

    /**
     * Reads any char not inside the specified [Set].
     */
    internal fun readNegativeAnyChar(chars: Set<Char>): Char? {
        val rChar = checkNegativeAnyChar(chars) ?: return null
        reader.advance()
        return rChar
    }

    /**
     * Reads any of the chars inside the specified [List] of [CharRange].
     * This function does not consume any character.
     */
    internal fun checkAnyChar(chars: List<CharRange>): Char? {
        val rChar = reader.currentChar()
        if (rChar != null) {
            val result = chars.binarySearch {
                when {
                    rChar < it.first -> 1
                    rChar > it.last -> -1
                    else -> 0
                }
            }

            if (result >= 0) {
                return rChar
            }
        }

        return null
    }

    /**
     * Reads any of the chars inside the specified [List] of [CharRange].
     */
    internal fun readAnyChar(chars: List<CharRange>): Char? {
        val rChar = checkAnyChar(chars) ?: return null
        reader.advance()
        return rChar
    }

    /**
     * Reads any char not inside the specified [List] of [CharRange].
     * This function does not consume any character.
     */
    internal fun checkNegativeAnyChar(chars: List<CharRange>): Char? {
        if (!reader.isEnd() && checkAnyChar(chars) == null) {
            return reader.currentChar()
        }
        return null
    }

    /**
     * Reads any char not inside the specified [List] of [CharRange].
     */
    internal fun readNegativeAnyChar(chars: List<CharRange>): Char? {
        val rChar = checkNegativeAnyChar(chars) ?: return null
        reader.advance()
        return rChar
    }

    /**
     * Reads a specific text from the reader.
     * This function does not consume any character.
     */
    internal fun checkText(text: String): Boolean {
        val initCursor = reader.saveCursor()
        val result = readText(text)
        initCursor.restore()
        return result
    }

    /**
     * Reads a specific text from the reader.
     */
    internal fun readText(text: String): Boolean {
        val cursor = reader.saveCursor()

        for (ch in text) {
            val rChar = reader.currentChar()
            if (rChar == null || rChar != ch) {
                cursor.restore()
                return false
            }

            reader.advance()
        }

        return true
    }

    /**
     * Reads any of the specified texts.
     * This function does not consume any character.
     */
    internal fun checkAnyText(texts: List<String>): String? {
        val initCursor = reader.saveCursor()
        val result = readAnyText(texts)
        initCursor.restore()
        return result
    }

    /**
     * Reads any of the specified texts.
     * This function does not consume any character.
     */
    internal fun checkNegativeAnyText(texts: List<String>) = checkAnyText(texts) == null

    /**
     * Reads any of the specified texts.
     */
    internal fun readAnyText(texts: List<String>): String? {
        for (text in texts) {
            if (readText(text)) {
                return text
            }
        }

        return null
    }

    /**
     * Returns a subsection of the reader.
     */
    internal fun substring(from: ITextReaderCursor, to: ITextReaderCursor): String {
        val currentPosition = reader.saveCursor()
        val length = to.position() - from.position()

        from.restore()
        val sb = StringBuilder(length)
        repeat(length) {
            sb.append(reader.currentChar())
            reader.advance()
        }

        currentPosition.restore()
        return sb.toString()
    }

    /**
     * Adds a node in the buffer.
     */
    internal fun addInBuffer(node: ParserNode) = forwardBuffer?.add(node)

    /**
     * Gets a value from the buffer and executes an action.
     */
    internal fun <T> fromBuffer(position: Int, type: NodeType): T? {
        val res = forwardBuffer?.find(position, type) ?: return null
        res.to.restore()
        @Suppress("UNCHECKED_CAST") return res as T
    }

    /**
     * Removes range of positions in the buffer.
     */
    internal fun removeTreeFromBuffer(node: ParserNode) =
            forwardBuffer?.remove(node.from.position(), node.to.position())

    /**
     * Parses the specified content with the Lexem grammar.
     */
    internal fun parse() = NumberNode.parseAnyNumberDefaultDecimal(this)

    /**
     * Finalize a node, setting its points
     */
    internal fun <T : ParserNode> finalizeNode(node: T, initCursor: ITextReaderCursor): T {
        val finalCursor = reader.saveCursor()

        node.from = initCursor
        node.to = finalCursor

        addInBuffer(node)
        return node
    }

    // STATIC -----------------------------------------------------------------

    companion object {
        /**
         * Parses a Lexem file.
         */
        internal fun parse(file: File, config: AngmarConfig): NumberNode? {
            if (!file.exists()) {
                throw AngmarIOException("The file ${file.canonicalPath} does not exist. Parsing aborted.")
            }

            val reader = CustomStringReader.from(file)
            return LexemParser(reader, config).parse()
        }
    }
}