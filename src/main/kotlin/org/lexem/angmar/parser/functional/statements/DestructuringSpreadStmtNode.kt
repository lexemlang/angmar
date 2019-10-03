package org.lexem.angmar.parser.functional.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.functional.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*


/**
 * Parser for spread element of destructuring.
 */
internal class DestructuringSpreadStmtNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var identifier: IdentifierNode
    var isConstant = false

    override fun toString() = StringBuilder().apply {
        append(spreadToken)
        if (isConstant) {
            append(constantToken)
        }
        append(identifier)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("identifier", identifier.toTree())
        result.addProperty("isConstant", isConstant)

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            DestructuringSpreadStmtAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val signalEndIdentifier = 1
        const val spreadToken = GlobalCommons.spreadOperator
        const val constantToken = GlobalCommons.constantToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses for a spread element of destructuring.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): DestructuringSpreadStmtNode? {
            parser.fromBuffer(parser.reader.currentPosition(), DestructuringSpreadStmtNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = DestructuringSpreadStmtNode(parser, parent, parentSignal)

            if (!parser.readText(spreadToken)) {
                return null
            }

            result.isConstant = parser.readText(constantToken)

            result.identifier =
                    IdentifierNode.parse(parser, result, signalEndIdentifier) ?: throw AngmarParserException(
                            AngmarParserExceptionType.DestructuringSpreadStatementWithoutIdentifierAfterSpreadOperator,
                            "An identifier was expected after spread operator '$spreadToken'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message = "Try adding an identifier here here"
                        }
                    }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
