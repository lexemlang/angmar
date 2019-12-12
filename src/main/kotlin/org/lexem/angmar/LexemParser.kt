package org.lexem.angmar

import org.lexem.angmar.io.*
import org.lexem.angmar.parser.*

/**
 * Lexer and parser for the Lexem language.
 */
class LexemParser constructor(val reader: ITextReader, forwardBuffer: Boolean = true) {
    private val forwardBuffer = if (forwardBuffer) {
        ForwardBuffer()
    } else {
        null
    }

    internal var isDescriptiveCode = false
    internal var isFilterCode = false

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
    internal fun checkAnyText(texts: Sequence<String>): String? {
        val initCursor = reader.saveCursor()
        val result = readAnyText(texts)
        initCursor.restore()
        return result
    }

    /**
     * Reads any of the specified texts.
     * This function does not consume any character.
     */
    internal fun checkNegativeAnyText(texts: Sequence<String>) = checkAnyText(texts) == null

    /**
     * Reads any of the specified texts.
     */
    internal fun readAnyText(texts: Sequence<String>): String? {
        for (text in texts) {
            if (readText(text)) {
                return text
            }
        }

        return null
    }

    /**
     * Adds a node in the buffer.
     */
    internal fun addInBuffer(node: ParserNode) = forwardBuffer?.add(node)

    /**
     * Gets a value from the buffer and executes an action.
     */
    fun <T : ParserNode> fromBuffer(position: Int, type: Class<T>): T? {
        val res = forwardBuffer?.find(position, type) ?: return null
        res.to.restore()
        return res
    }

    /**
     * Finalize a node, setting its bounds and adding it to the forward buffer.
     */
    internal fun <T : ParserNode> finalizeNode(node: T, initCursor: ITextReaderCursor): T {
        finalizeNodeNoBuffer(node, initCursor)
        addInBuffer(node)
        return node
    }

    /**
     * Finalize a node setting its bounds.
     */
    internal fun <T : ParserNode> finalizeNodeNoBuffer(node: T, initCursor: ITextReaderCursor): T {
        val finalCursor = reader.saveCursor()

        node.from = initCursor
        node.to = finalCursor

        return node
    }
}
