package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for variable declaration.
 */
class VarDeclarationStmtNode private constructor(parser: LexemParser, val identifier: ParserNode,
        val value: ParserNode) : ParserNode(parser) {
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        append(if (isConstant) {
            constKeyword
        } else {
            variableKeyword
        })
        append(' ')
        append(identifier)
        append(" $assignOperator ")
        append(value)
    }.toString()

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("isConstant", isConstant)
        printer.addField("identifier", identifier)
        printer.addField("value", value)
    }

    companion object {
        const val constKeyword = "let"
        const val variableKeyword = "var"
        const val assignOperator = AssignOperatorNode.assignOperator


        // METHODS ------------------------------------------------------------

        /**
         * Parses a variable declaration.
         */
        fun parse(parser: LexemParser): VarDeclarationStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), VarDeclarationStmtNode::class.java)?.let {
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()

            var keywordType = "variable"
            var declarationKeyword = variableKeyword
            val isConstant = when {
                Commons.parseKeyword(parser, constKeyword) -> {
                    declarationKeyword = constKeyword
                    keywordType = "constant"
                    true
                }
                Commons.parseKeyword(parser, variableKeyword) -> false
                else -> return null
            }

            WhitespaceNode.parse(parser)

            val identifier = DestructuringStmtNode.parse(parser) ?: Commons.parseDynamicIdentifier(parser)
            ?: throw AngmarParserException(AngmarParserExceptionType.VarDeclarationStatementWithoutIdentifier,
                    "An identifier or destructuring was expected after the $keywordType declaration keyword '$declarationKeyword'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding a whitespace followed by an identifier here, e.g. ' ${GlobalCommons.wildcardVariable}'")
                }
            }

            WhitespaceNode.parse(parser)

            if (!parser.readText(assignOperator)) {
                throw AngmarParserException(AngmarParserExceptionType.VarDeclarationStatementWithoutAssignOperator,
                        "A $keywordType declaration requires a value so it is required the assign operator '$assignOperator'.") {
                    addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                        title(Consts.Logger.codeTitle)
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(parser.reader.readAllText(), null) {
                        title(Consts.Logger.hintTitle)
                        highlightCursorAt(parser.reader.currentPosition())
                        message("Try adding the assign operator '$assignOperator' here followed by an expression")
                    }
                }
            }

            WhitespaceNode.parse(parser)

            val value = ExpressionsCommons.parseExpression(parser) ?: throw AngmarParserException(
                    AngmarParserExceptionType.VarDeclarationStatementWithoutExpressionAfterAssignOperator,
                    "An expression was expected after assign operator '$assignOperator'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an expression here")
                }
            }

            val result = VarDeclarationStmtNode(parser, identifier, value)
            result.isConstant = isConstant

            return parser.finalizeNode(result, initCursor)
        }
    }
}
