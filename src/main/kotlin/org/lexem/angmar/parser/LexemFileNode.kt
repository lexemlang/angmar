package org.lexem.angmar.parser

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for Lexem files.
 */
internal class LexemFileNode private constructor(parser: LexemParser) : ParserNode(parser, null) {
    val statements = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(statements.joinToString("\n    "))
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("statements", SerializationUtils.listToTest(statements))

        return result
    }

    // It must never be called like this.
    override fun compile(parent: CompiledNode, parentSignal: Int) = throw AngmarUnreachableException()

    companion object {
        /**
         * Parses a Lexem file.
         */
        fun parse(parser: LexemParser): LexemFileNode? {
            val initCursor = parser.reader.saveCursor()

            if (!parser.reader.isStart()) {
                return null
            }

            val result = LexemFileNode(parser)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val statement = StatementCommons.parseAnyStatement(parser, result)
                if (statement == null) {
                    initLoopCursor.restore()
                    break
                }

                result.statements.add(statement)
            }

            WhitespaceNode.parse(parser)

            if (!parser.reader.isEnd()) {
                throw AngmarParserException(AngmarParserExceptionType.LexemFileEOFExpected,
                        "Lexem files must end with an end of file (EOF).") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.codeTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "The end of file (EOF) was expected here"
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(parser.reader.currentPosition(), parser.reader.readAllText().length - 1)
                        message = "Try removing the rest of the file"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
