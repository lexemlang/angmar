package org.lexem.angmar.parser.functional.statements.selective

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.selective.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for cases of the selective statements.
 */
internal class SelectiveCaseStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    var patterns = mutableListOf<ParserNode>()
    lateinit var block: ParserNode

    override fun toString() = StringBuilder().apply {
        append(patterns.joinToString("$patternSeparator "))
        append(' ')
        append(block)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("patterns", SerializationUtils.listToTest(patterns))
        result.add("block", block.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            SelectiveCaseStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        const val patternSeparator = GlobalCommons.elementSeparator


        // METHODS ------------------------------------------------------------

        /**
         * Parses a case of the selective statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode): SelectiveCaseStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = SelectiveCaseStmtNode(parser, parent)

            // Patterns
            var pattern =
                    ConditionalPatternSelectiveStmtNode.parse(parser, result) ?: ElsePatternSelectiveStmtNode.parse(
                            parser, result) ?: VarPatternSelectiveStmtNode.parse(parser, result)
                    ?: ExpressionPatternSelectiveStmtNode.parse(parser, result) ?: return null

            result.patterns.add(pattern)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val preSeparatorCursor = parser.reader.saveCursor()
                if (!parser.readText(patternSeparator)) {
                    initLoopCursor.restore()
                    break
                }

                WhitespaceNode.parse(parser)

                pattern =
                        ConditionalPatternSelectiveStmtNode.parse(parser, result) ?: ElsePatternSelectiveStmtNode.parse(
                                parser, result) ?: VarPatternSelectiveStmtNode.parse(parser, result)
                                ?: ExpressionPatternSelectiveStmtNode.parse(parser, result)
                                ?: throw AngmarParserException(
                                AngmarParserExceptionType.SelectiveCaseStatementWithoutPatternAfterElementSeparator,
                                "A pattern was expected after the pattern separator '$patternSeparator'.") {
                            val fullText = parser.reader.readAllText()
                            addSourceCode(fullText, parser.reader.getSource()) {
                                title = Consts.Logger.codeTitle
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(fullText, null) {
                                title = Consts.Logger.hintTitle
                                highlightSection(preSeparatorCursor.position())
                                message = "Try removing the pattern separator '$patternSeparator'"
                            }
                        }

                result.patterns.add(pattern)
            }

            WhitespaceNode.parse(parser)

            result.block = BlockStmtNode.parse(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.SelectiveCaseStatementWithoutBlock,
                    "A block was expected after the patterns of the case to be executed when any of the patterns match.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message = "Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
