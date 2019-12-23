package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.expressions.*


/**
 * Parser for variable declaration.
 */
internal class VarDeclarationStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var identifier: ParserNode
    lateinit var value: ParserNode
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

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isConstant", isConstant)
        result.add("identifier", identifier.toTree())
        result.add("value", value.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            VarDeclarationStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        const val constKeyword = "let"
        const val variableKeyword = "var"
        const val assignOperator = AssignOperatorNode.assignOperator


        // METHODS ------------------------------------------------------------

        /**
         * Parses a variable declaration.
         */
        fun parse(parser: LexemParser, parent: ParserNode): VarDeclarationStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = VarDeclarationStmtNode(parser, parent)

            var keywordType = "variable"
            var declarationKeyword = variableKeyword
            result.isConstant = when {
                Commons.parseKeyword(parser, constKeyword) -> {
                    declarationKeyword = constKeyword
                    keywordType = "constant"
                    true
                }
                Commons.parseKeyword(parser, variableKeyword) -> false
                else -> return null
            }

            WhitespaceNode.parse(parser)

            result.identifier =
                    DestructuringStmtNode.parse(parser, result) ?: Commons.parseDynamicIdentifier(parser, result)
                            ?: throw AngmarParserException(
                            AngmarParserExceptionType.VarDeclarationStatementWithoutIdentifier,
                            "An identifier or destructuring was expected after the $keywordType declaration keyword '$declarationKeyword'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message =
                                    "Try adding a whitespace followed by an identifier here, e.g. ' ${GlobalCommons.wildcardVariable}'"
                        }
                    }

            WhitespaceNode.parse(parser)

            if (!parser.readText(assignOperator)) {
                throw AngmarParserException(AngmarParserExceptionType.VarDeclarationStatementWithoutAssignOperator,
                        "A $keywordType declaration requires a value so it is required the assign operator '$assignOperator'.") {
                    val fullText = parser.reader.readAllText()
                    addSourceCode(fullText, parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightCursorAt(parser.reader.currentPosition())
                        message = "Try adding the assign operator '$assignOperator' here followed by an expression"
                    }
                }
            }

            WhitespaceNode.parse(parser)

            result.value = ExpressionsCommons.parseExpression(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.VarDeclarationStatementWithoutExpressionAfterAssignOperator,
                    "An expression was expected after assign operator '$assignOperator'.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message = "Try adding an expression here"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
