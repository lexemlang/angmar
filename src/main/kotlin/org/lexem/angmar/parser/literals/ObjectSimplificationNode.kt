package org.lexem.angmar.parser.literals

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.literals.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.functional.statements.*


/**
 * Parser for simplification elements of object literals.
 */
internal class ObjectSimplificationNode private constructor(parser: LexemParser, parent: ParserNode,
        parentSignal: Int) : ParserNode(parser, parent, parentSignal) {
    lateinit var identifier: ParserNode
    var isConstant = false
    var parameterList: FunctionParameterListNode? = null
    var block: ParserNode? = null

    override fun toString() = StringBuilder().apply {
        if (isConstant) {
            append(constantToken)
        }
        append(identifier)

        if (parameterList != null) {
            append(parameterList)
            append(block)
        }
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.addProperty("isConstant", isConstant)
        result.add("identifier", identifier.toTree())
        result.add("parameterList", parameterList?.toTree())
        result.add("block", block?.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            ObjectSimplificationAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val constantToken = GlobalCommons.constantToken


        // METHODS ------------------------------------------------------------

        /**
         * Parses a simplification element of object literal.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): ObjectSimplificationNode? {
            val initCursor = parser.reader.saveCursor()
            val result = ObjectSimplificationNode(parser, parent, parentSignal)

            result.isConstant = parser.readText(constantToken)
            result.identifier =
                    Commons.parseDynamicIdentifier(parser, result, ObjectSimplificationAnalyzer.signalEndIdentifier)
                            ?: let {
                                initCursor.restore()
                                return@parse null
                            }

            val preWSOfArgumentListCursor = parser.reader.saveCursor()

            WhitespaceNode.parse(parser)

            result.parameterList =
                    FunctionParameterListNode.parse(parser, result, ObjectSimplificationAnalyzer.signalEndParameterList)
            if (result.parameterList != null) {
                WhitespaceNode.parse(parser)

                val keepIsDescriptiveCode = parser.isDescriptiveCode
                parser.isDescriptiveCode = false
                result.block = BlockStmtNode.parse(parser, result, ObjectSimplificationAnalyzer.signalEndBlock)
                        ?: throw AngmarParserException(AngmarParserExceptionType.ObjectSimplificationWithoutBlock,
                                "A block of code was expected after the argument list.") {
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
