package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for function statement.
 */
class FunctionStmtNode private constructor(parser: LexemParser, val name: IdentifierNode, val block: ParserNode) :
        ParserNode(parser) {
    var argumentList: FunctionArgumentListNode? = null

    override fun toString() = StringBuilder().apply {
        append(keyword)
        append(' ')
        append(name)

        if (argumentList != null) {
            append(' ')
            append(argumentList)
        }

        append(' ')
        append(block)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addOptionalField("arguments", argumentList)
        printer.addField("block", block)
    }

    companion object {
        const val keyword = FunctionNode.keyword


        // METHODS ------------------------------------------------------------

        /**
         * Parses a function statement.
         */
        fun parse(parser: LexemParser): FunctionStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), FunctionStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            if (!Commons.parseKeyword(parser, keyword)) {
                return null
            }

            WhitespaceNode.parse(parser)

            val name = IdentifierNode.parse(parser) ?: let {
                initCursor.restore()
                return@parse null
            }

            WhitespaceNode.parse(parser)

            val argumentList = FunctionArgumentListNode.parse(parser)
            if (argumentList != null) {
                WhitespaceNode.parse(parser)
            }

            val keepIsDescriptiveCode = parser.isDescriptiveCode
            parser.isDescriptiveCode = false

            val block = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.FunctionStatementWithoutBlock,
                    "A block of code was expected after the '$keyword' keyword.") {
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

            parser.isDescriptiveCode = keepIsDescriptiveCode

            val result = FunctionStmtNode(parser, name, block)
            result.argumentList = argumentList
            return parser.finalizeNode(result, initCursor)
        }
    }
}
