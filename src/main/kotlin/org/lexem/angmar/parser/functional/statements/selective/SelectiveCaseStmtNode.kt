package org.lexem.angmar.parser.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for cases of the selective statements.
 */
class SelectiveCaseStmtNode private constructor(parser: LexemParser, val patterns: List<ParserNode>,
        val block: ParserNode) : ParserNode(parser) {

    override fun toString() = StringBuilder().apply {
        append(patterns.joinToString("$elementSeparator "))
        append(' ')
        append(block)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("patterns", patterns)
        printer.addField("block", block)
    }

    companion object {
        const val elementSeparator = GlobalCommons.elementSeparator


        // METHODS ------------------------------------------------------------

        /**
         * Parses a case of the selective statements.
         */
        fun parse(parser: LexemParser): SelectiveCaseStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), SelectiveCaseStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            val patterns = mutableListOf<ParserNode>()

            // Patterns
            var pattern =
                    ConditionalPatternSelectiveStmtNode.parse(parser) ?: ElsePatternSelectiveStmtNode.parse(parser)
                    ?: VarPatternSelectiveStmtNode.parse(parser) ?: ExpressionPatternSelectiveStmtNode.parse(parser)
                    ?: return null

            patterns.add(pattern)

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val preSeparatorCursor = parser.reader.saveCursor()
                if (!parser.readText(elementSeparator)) {
                    initLoopCursor.restore()
                    break
                }

                WhitespaceNode.parse(parser)

                pattern =
                        ConditionalPatternSelectiveStmtNode.parse(parser) ?: ElsePatternSelectiveStmtNode.parse(parser)
                                ?: VarPatternSelectiveStmtNode.parse(parser)
                                ?: ExpressionPatternSelectiveStmtNode.parse(parser) ?: throw AngmarParserException(
                                AngmarParserExceptionType.SelectiveCaseStatementWithoutPatternAfterElementSeparator,
                                "A pattern was expected after the pattern separator '$elementSeparator'.") {
                            addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                                title(Consts.Logger.codeTitle)
                                highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                            }
                            addSourceCode(parser.reader.readAllText(), null) {
                                title(Consts.Logger.hintTitle)
                                highlightSection(preSeparatorCursor.position())
                                message("Try removing the pattern separator '$elementSeparator'")
                            }
                        }

                patterns.add(pattern)
            }

            WhitespaceNode.parse(parser)

            val block = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.SelectiveCaseStatementWithoutBlock,
                    "A block was expected after the patterns of the case to be executed when any of the patterns match.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here")
                }
            }

            val result = SelectiveCaseStmtNode(parser, patterns, block)
            return parser.finalizeNode(result, initCursor)
        }
    }
}
