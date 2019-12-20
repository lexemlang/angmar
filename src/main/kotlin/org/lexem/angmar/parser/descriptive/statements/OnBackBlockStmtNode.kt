package org.lexem.angmar.parser.descriptive.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for onBack block statements.
 */
internal class OnBackBlockStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    var parameters: FunctionParameterListNode? = null
    lateinit var block: ParserNode

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(' ')
        append(block)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("block", block.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            OnBackBlockStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val macroName = "onBack${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses an onBack block statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): OnBackBlockStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = OnBackBlockStmtNode(parser, parent, parentSignal)

            if (!parser.readText(macroName)) {
                return null
            }

            WhitespaceNode.parse(parser)

            result.parameters =
                    FunctionParameterListNode.parse(parser, result, OnBackBlockStmtAnalyzer.signalEndParameters)

            if (result.parameters != null) {
                WhitespaceNode.parse(parser)
            }

            result.block = BlockStmtNode.parse(parser, result, OnBackBlockStmtAnalyzer.signalEndBlock)
                    ?: ConditionalStmtNode.parse(parser, result, OnBackBlockStmtAnalyzer.signalEndBlock)
                            ?: SelectiveStmtNode.parse(parser, result, OnBackBlockStmtAnalyzer.signalEndBlock)
                            ?: throw AngmarParserException(AngmarParserExceptionType.OnBackBlockWithoutBlock,
                            "The onBack block requires a block that contains its code.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightSection(initCursor.position(), initCursor.position() + macroName.length - 1)
                            message = "Try removing the '$macroName' keyword"
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
