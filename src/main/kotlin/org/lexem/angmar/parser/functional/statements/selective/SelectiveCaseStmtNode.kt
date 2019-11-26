package org.lexem.angmar.parser.functional.statements.selective

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.selective.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for cases of the selective statements.
 */
internal class SelectiveCaseStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
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

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            SelectiveCaseStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val patternSeparator = GlobalCommons.elementSeparator


        // METHODS ------------------------------------------------------------

        /**
         * Parses a case of the selective statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): SelectiveCaseStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), SelectiveCaseStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = SelectiveCaseStmtNode(parser, parent, parentSignal)

            // Patterns
            var pattern = ConditionalPatternSelectiveStmtNode.parse(parser, result,
                    result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern)
                    ?: ElsePatternSelectiveStmtNode.parse(parser, result,
                            result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern)
                    ?: VarPatternSelectiveStmtNode.parse(parser, result,
                            result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern)
                    ?: ExpressionPatternSelectiveStmtNode.parse(parser, result,
                            result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern) ?: return null

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

                pattern = ConditionalPatternSelectiveStmtNode.parse(parser, result,
                        result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern)
                        ?: ElsePatternSelectiveStmtNode.parse(parser, result,
                                result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern)
                                ?: VarPatternSelectiveStmtNode.parse(parser, result,
                                result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern)
                                ?: ExpressionPatternSelectiveStmtNode.parse(parser, result,
                                result.patterns.size + SelectiveCaseStmtAnalyzer.signalEndFirstPattern)
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

            result.block = BlockStmtNode.parse(parser, result, SelectiveCaseStmtAnalyzer.signalEndBlock)
                    ?: throw AngmarParserException(AngmarParserExceptionType.SelectiveCaseStatementWithoutBlock,
                            "A block was expected after the patterns of the case to be executed when any of the patterns match.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message =
                                    "Try adding an empty block '${BlockStmtNode.startToken}${BlockStmtNode.endToken}' here"
                        }
                    }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
