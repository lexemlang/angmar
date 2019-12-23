package org.lexem.angmar.parser.functional.statements.selective

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.functional.statements.selective.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for variable patterns of the selective statements.
 */
internal class VarPatternSelectiveStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var identifier: ParserNode
    var conditional: ConditionalPatternSelectiveStmtNode? = null
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        append(if (isConstant) {
            constKeyword
        } else {
            variableKeyword
        })

        append(' ')
        append(identifier)

        if (conditional != null) {
            append(' ')
            append(conditional)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isConstant", isConstant)
        result.add("identifier", identifier.toTree())
        result.add("conditional", conditional?.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            VarPatternSelectiveStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        const val constKeyword = VarDeclarationStmtNode.constKeyword
        const val variableKeyword = VarDeclarationStmtNode.variableKeyword


        // METHODS ------------------------------------------------------------

        /**
         * Parses a variable patterns of the selective statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode): VarPatternSelectiveStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = VarPatternSelectiveStmtNode(parser, parent)

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

            result.identifier =
                    DestructuringStmtNode.parse(parser, result) ?: Commons.parseDynamicIdentifier(parser, result)
                            ?: throw AngmarParserException(
                            AngmarParserExceptionType.VarPatternSelectiveStatementWithoutIdentifier,
                            "An identifier or destructuring was expected after the $keywordType declaration keyword '$declarationKeyword'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding an identifier here, e.g. '${GlobalCommons.wildcardVariable}'"
                        }
                    }

            result.isConstant = isConstant

            // conditional
            let {
                val initConditionalCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                result.conditional = ConditionalPatternSelectiveStmtNode.parse(parser, result)
                if (result.conditional == null) {
                    initConditionalCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
