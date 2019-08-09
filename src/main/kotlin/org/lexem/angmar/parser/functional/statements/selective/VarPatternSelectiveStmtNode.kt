package org.lexem.angmar.parser.functional.statements.selective

import org.lexem.angmar.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.printer.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for variable patterns of the selective statements.
 */
class VarPatternSelectiveStmtNode private constructor(parser: LexemParser, val identifier: ParserNode) :
        ParserNode(parser) {
    var isConstant = false
    var conditional: ConditionalPatternSelectiveStmtNode? = null

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

    override fun toTree(printer: TreeLikePrinter) {
        printer.addField("isConstant", isConstant)
        printer.addField("identifier", identifier)
        printer.addOptionalField("conditional", conditional)
    }

    companion object {
        const val constKeyword = VarDeclarationStmtNode.constKeyword
        const val variableKeyword = VarDeclarationStmtNode.variableKeyword


        // METHODS ------------------------------------------------------------

        /**
         * Parses a variable patterns of the selective statements.
         */
        fun parse(parser: LexemParser): VarPatternSelectiveStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), VarPatternSelectiveStmtNode::class.java)?.let {
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
            ?: throw AngmarParserException(AngmarParserExceptionType.VarPatternSelectiveStatementWithoutIdentifier,
                    "An identifier or destructuring was expected after the $keywordType declaration keyword '$declarationKeyword'.") {
                addSourceCode(parser.reader.readAllText(), parser.reader.getSource()) {
                    title(Consts.Logger.codeTitle)
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(parser.reader.readAllText(), null) {
                    title(Consts.Logger.hintTitle)
                    highlightCursorAt(parser.reader.currentPosition())
                    message("Try adding an identifier here, e.g. '${GlobalCommons.wildcardVariable}'")
                }
            }

            val result = VarPatternSelectiveStmtNode(parser, identifier)
            result.isConstant = isConstant

            // conditional
            let {
                val initConditionalCursor = parser.reader.saveCursor()

                WhitespaceNode.parse(parser)

                result.conditional = ConditionalPatternSelectiveStmtNode.parse(parser)
                if (result.conditional == null) {
                    initConditionalCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
