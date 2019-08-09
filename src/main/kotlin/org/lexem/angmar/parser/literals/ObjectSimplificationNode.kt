package org.lexem.angmar.parser.literals

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for simplification elements of object literals.
 */
class ObjectSimplificationNode private constructor(parser: LexemParser, val identifier: ParserNode) :
        ParserNode(parser) {
    var isConstant = false
    var arguments: FunctionArgumentListNode? = null
    var body: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        if (isConstant) {
            append(constantToken)
        }
        append(identifier)

        if (arguments != null) {
            append(arguments)
            append(body)
        }
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("isConstant", isConstant)
        printer.addField("identifier", identifier)
        printer.addOptionalField("argumentList", arguments)
        printer.addOptionalField("body", body)
    }

    companion object {
        const val constantToken = GlobalCommons.constantToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses a simplification element of object literal.
         */
        fun parse(parser: LexemParser): ObjectSimplificationNode? {
            parser.fromBuffer(parser.reader.currentPosition(), ObjectSimplificationNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val isConstant = parser.readText(constantToken)
            val identifier = Commons.parseDynamicIdentifier(parser) ?: let {
                initCursor.restore()
                return@parse null
            }

            val result = ObjectSimplificationNode(parser, identifier)
            result.isConstant = isConstant

            val preWSOfArgumentListCursor = parser.reader.saveCursor()

            WhitespaceNode.parse(parser)

            result.arguments = FunctionArgumentListNode.parse(parser)
            if (result.arguments != null) {
                WhitespaceNode.parse(parser)

                val keepIsDescriptiveCode = parser.isDescriptiveCode
                parser.isDescriptiveCode = false
                result.body = GlobalCommons.parseBlock(parser) ?: throw AngmarParserException(
                        AngmarParserExceptionType.ObjectSimplificationWithoutBlock,
                        "A block of code was expected after the argument list.") {
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
            } else {
                if (!parser.readText(ObjectNode.elementSeparator) && !parser.readText(ObjectNode.endToken)) {
                    initCursor.restore()
                    return null
                }

                preWSOfArgumentListCursor.restore()
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
