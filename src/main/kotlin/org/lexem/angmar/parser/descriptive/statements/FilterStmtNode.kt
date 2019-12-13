package org.lexem.angmar.parser.descriptive.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for filter statements.
 */
internal class FilterStmtNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var name: IdentifierNode
    lateinit var block: ParserNode
    var properties: PropertyStyleObjectBlockNode? = null
    var parameterList: FunctionParameterListNode? = null

    override fun toString() = StringBuilder().apply {
        append(keyword)
        append(' ')
        append(name)

        if (properties != null) {
            append(properties)
        }

        if (parameterList != null) {
            if (properties != null) {
                append(' ')
            }

            append(parameterList)
        }

        append(' ')
        append(block)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("properties", properties?.toTree())
        result.add("arguments", parameterList?.toTree())
        result.add("block", block.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) = FilterStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val keyword = "filter"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a filter statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): FilterStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FilterStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = FilterStmtNode(parser, parent, parentSignal)

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            WhitespaceNode.parse(parser)

            result.name = IdentifierNode.parse(parser, result, FilterStmtAnalyzer.signalEndName) ?: let {
                initCursor.restore()
                return@parse null
            }

            WhitespaceNode.parse(parser)

            result.properties =
                    PropertyStyleObjectBlockNode.parse(parser, result, FilterStmtAnalyzer.signalEndProperties)
            if (result.properties != null) {
                WhitespaceNode.parse(parser)
            }

            result.parameterList =
                    FunctionParameterListNode.parse(parser, result, FilterStmtAnalyzer.signalEndParameterList)
            if (result.parameterList != null) {
                WhitespaceNode.parse(parser)
            }

            val keepIsDescriptiveCode = parser.isDescriptiveCode
            val keepIsFilterCode = parser.isFilterCode
            parser.isDescriptiveCode = true
            parser.isFilterCode = true

            result.block =
                    BlockStmtNode.parse(parser, result, FilterStmtAnalyzer.signalEndBlock) ?: LambdaStmtNode.parse(
                            parser, result, FilterStmtAnalyzer.signalEndBlock) ?: throw AngmarParserException(
                            AngmarParserExceptionType.FilterStatementWithoutBlock, "Filters require a block of code.") {
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

            parser.isDescriptiveCode = keepIsDescriptiveCode
            parser.isFilterCode = keepIsFilterCode

            return parser.finalizeNode(result, initCursor)
        }
    }
}
