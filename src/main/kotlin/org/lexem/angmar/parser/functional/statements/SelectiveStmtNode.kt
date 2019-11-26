package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.selective.*


/**
 * Parser for selective statements.
 */
internal class SelectiveStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var condition: ParserNode? = null
    var tag: IdentifierNode? = null
    val cases = mutableListOf<ParserNode>()

    override fun toString() = StringBuilder().apply {
        append(keyword)
        append(' ')
        if (condition != null) {
            append(condition)
            append(' ')
        }

        append(startToken)
        if (tag != null) {
            append(tagPrefix)
            append(tag)
        }
        append('\n')
        append(cases.joinToString("\n    "))
        append('\n')
        append(endToken)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("condition", condition?.toTree())
        result.add("tag", tag?.toTree())
        result.add("cases", SerializationUtils.listToTest(cases))

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            SelectiveStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val keyword = "when"
        const val startToken = BlockStmtNode.startToken
        const val endToken = BlockStmtNode.endToken
        const val tagPrefix = GlobalCommons.tagPrefix

        // METHODS ------------------------------------------------------------

        /**
         * Parses a selective statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): SelectiveStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), SelectiveStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = SelectiveStmtNode(parser, parent, parentSignal)

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            WhitespaceNode.parse(parser)

            // condition
            result.condition = ParenthesisExpressionNode.parse(parser, result, SelectiveStmtAnalyzer.signalEndCondition)
            if (result.condition != null) {
                WhitespaceNode.parse(parser)
            }

            if (!parser.readText(startToken)) {
                throw AngmarParserException(AngmarParserExceptionType.SelectiveStatementWithoutStartToken,
                        "An open bracket '$startToken' was expected to open the block that will contain the cases of the selective statement.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the open bracket '$startToken' here"
                    }
                }
            }

            // tag
            let {
                val initTagCursor = parser.reader.saveCursor()

                if (!parser.readText(tagPrefix)) {
                    return@let
                }

                result.tag = IdentifierNode.parse(parser, result, SelectiveStmtAnalyzer.signalEndTag)
                if (result.tag == null) {
                    initTagCursor.restore()
                }
            }

            while (true) {
                val initLoopCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                val case = SelectiveCaseStmtNode.parse(parser, result,
                        result.cases.size + SelectiveStmtAnalyzer.signalEndFirstCase)
                if (case == null) {
                    initLoopCursor.restore()
                    break
                }

                result.cases.add(case)
            }

            if (result.cases.isEmpty()) {
                throw AngmarParserException(AngmarParserExceptionType.SelectiveStatementWithoutAnyCase,
                        "Selective statements require at least one case.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding a case here"
                    }
                    addNote(Consts.Logger.hintTitle, "Try removing the whole selective statement")
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(endToken)) {
                throw AngmarParserException(AngmarParserExceptionType.SelectiveStatementWithoutEndToken,
                        "The close bracket was expected '$endToken' to finish the selective statement.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the close bracket '$endToken' here"
                    }
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
