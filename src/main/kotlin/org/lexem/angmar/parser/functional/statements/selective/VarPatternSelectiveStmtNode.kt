package org.lexem.angmar.parser.functional.statements.selective

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.selective.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for variable patterns of the selective statements.
 */
internal class VarPatternSelectiveStmtNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var identifier: ParserNode
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

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isConstant", isConstant)
        result.add("identifier", identifier.toTree())
        result.add("conditional", conditional?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            VarPatternSelectiveStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val constKeyword = VarDeclarationStmtNode.constKeyword
        const val variableKeyword = VarDeclarationStmtNode.variableKeyword


        // METHODS ------------------------------------------------------------

        /**
         * Parses a variable patterns of the selective statements.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): VarPatternSelectiveStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = VarPatternSelectiveStmtNode(parser, parent, parentSignal)

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
                    DestructuringStmtNode.parse(parser, result, VarPatternSelectiveStmtAnalyzer.signalEndIdentifier)
                            ?: Commons.parseDynamicIdentifier(parser, result,
                                    VarPatternSelectiveStmtAnalyzer.signalEndIdentifier) ?: throw AngmarParserException(
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

                result.conditional = ConditionalPatternSelectiveStmtNode.parse(parser, result,
                        VarPatternSelectiveStmtAnalyzer.signalEndConditional)
                if (result.conditional == null) {
                    initConditionalCursor.restore()
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
