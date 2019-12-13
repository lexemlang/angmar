package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for lambda statements.
 */
internal class LambdaStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var statement: ParserNode

    override fun toString() = StringBuilder().apply {
        append("$token $statement")
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("statement", statement.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = LambdaStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val token = "="

        // METHODS ------------------------------------------------------------

        /**
         * Parses a lambda statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): LambdaStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), LambdaStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!parser.readText(token)) {
                return null
            }

            val result = LambdaStmtNode(parser, parent, parentSignal)

            WhitespaceNode.parse(parser)

            val statement = if (parser.isDescriptiveCode) {
                // Descriptive code
                LexemePatternContentNode.parse(parser, result, LambdaStmtAnalyzer.signalEndExpression)
            } else {
                // Functional code
                ExpressionsCommons.parseExpression(parser, result, LambdaStmtAnalyzer.signalEndExpression)
            }
            if (statement == null) {
                val mainMessage = if (parser.isDescriptiveCode) {
                    // Descriptive code
                    "A valid pattern was expected after the token '$token'."
                } else {
                    // Functional code
                    "A valid expression was expected after the token '$token'."
                }

                throw AngmarParserException(AngmarParserExceptionType.LambdaStatementWithoutExpression, mainMessage) {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = if (parser.isDescriptiveCode) {
                            // Descriptive code
                            "Try adding some lexemes here"
                        } else {
                            // Functional code
                            "Try adding an expression here"
                        }
                    }
                }
            }
            result.statement = statement

            return parser.finalizeNode(result, initCursor)
        }
    }
}
