package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.macros.*


/**
 * Parser for public macro statement.
 */
internal class PublicMacroStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var element: ParserNode

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(' ')
        append(element)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("element", element.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            PublicMacroStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val macroName = "pub${MacroExpressionNode.macroSuffix}"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a public macro statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): PublicMacroStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), PublicMacroStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = PublicMacroStmtNode(parser, parent, parentSignal)

            if (!parser.readText(macroName)) {
                return null
            }

            WhitespaceNode.parse(parser)

            result.element = StatementCommons.parseAnyPublicMacroStatement(parser, result,
                    PublicMacroStmtAnalyzer.signalEndElement) ?: throw AngmarParserException(
                    AngmarParserExceptionType.PublicMacroStatementWithoutValidStatement,
                    "A valid statement was expected after the '$macroName' macro.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(initCursor.position(), initCursor.position() + macroName.length - 1)
                    message = "Try removing the '$macroName' macro"
                }
                addNote(Consts.Logger.hintTitle,
                        "The valid statements are variable, function, expression or filter declarations.")
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
