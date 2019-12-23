package org.lexem.angmar.parser.descriptive.statements

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.descriptive.statements.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for set properties macro statement.
 */
internal class SetPropsMacroStmtNode private constructor(parser: LexemParser, parent: ParserNode) :
        ParserNode(parser, parent) {
    lateinit var properties: PropertyStyleObjectBlockNode

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(properties)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("properties", properties.toTree())

        return result
    }

    override fun compile(parent: CompiledNode, parentSignal: Int) =
            SetPropsMacroStmtCompiled.compile(parent, parentSignal, this)

    companion object {
        const val macroName = "set_props${MacroExpressionNode.macroSuffix}"


        // METHODS ------------------------------------------------------------

        /**
         * Parses a set properties macro statement.
         */
        fun parse(parser: LexemParser, parent: ParserNode): SetPropsMacroStmtNode? {
            val initCursor = parser.reader.saveCursor()
            val result = SetPropsMacroStmtNode(parser, parent)

            if (!parser.readText(macroName)) {
                return null
            }

            result.properties = PropertyStyleObjectBlockNode.parse(parser, result) ?: throw AngmarParserException(
                    AngmarParserExceptionType.SetPropsMacroStatementWithoutPropertyStyleObject,
                    "A property-style object was expected after the '$macroName' macro.") {
                val fullText = parser.reader.readAllText()
                addSourceCode(fullText, parser.reader.getSource()) {
                    title = Consts.Logger.codeTitle
                    highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightCursorAt(parser.reader.currentPosition())
                    message =
                            "Try adding an empty property-style object here '${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.endToken}'"
                }
                addSourceCode(fullText, null) {
                    title = Consts.Logger.hintTitle
                    highlightSection(initCursor.position(), initCursor.position() + macroName.length - 1)
                    message = "Try removing the '$macroName' macro"
                }
            }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
