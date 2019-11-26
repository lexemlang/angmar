package org.lexem.angmar.parser.descriptive.expressions.macros

import com.google.gson.*
import org.lexem.angmar.*
import org.lexem.angmar.analyzer.nodes.descriptive.expressions.macros.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import org.lexem.angmar.parser.literals.*


/**
 * Parser for macro 'check props'.
 */
internal class MacroCheckPropsNode private constructor(parser: LexemParser, parent: ParserNode, parentSignal: Int) :
        ParserNode(parser, parent, parentSignal) {
    lateinit var value: PropertyStyleObjectBlockNode

    override fun toString() = StringBuilder().apply {
        append(macroName)
        append(value)
    }.toString()

    override fun toTree(): JsonObject {
        val result = super.toTree()

        result.add("value", value.toTree())

        return result
    }

    override fun analyze(analyzer: LexemAnalyzer, signal: Int) =
            MacroCheckPropsAnalyzer.stateMachine(analyzer, signal, this)

    companion object {
        const val signalEndValue = 1
        const val macroName = "check_props${MacroExpressionNode.macroSuffix}"

        // METHODS ------------------------------------------------------------

        /**
         * Parses a macro 'check props'.
         */
        fun parse(parser: LexemParser, parent: ParserNode, parentSignal: Int): MacroCheckPropsNode? {
            parser.fromBuffer(parser.reader.currentPosition(), MacroCheckPropsNode::class.java)?.let {
                it.parent = parent
                it.parentSignal = parentSignal
                return@parse it
            }

            val initCursor = parser.reader.saveCursor()
            val result = MacroCheckPropsNode(parser, parent, parentSignal)

            if (!parser.readText(macroName)) {
                return null
            }

            result.value =
                    PropertyStyleObjectBlockNode.parse(parser, result, signalEndValue) ?: throw AngmarParserException(
                            AngmarParserExceptionType.MacroCheckPropsWithoutPropertyStyleBlockAfterMacroName,
                            "A property-style block was expected after the macro name '$macroName'.") {
                        val fullText = parser.reader.readAllText()
                        addSourceCode(fullText, parser.reader.getSource()) {
                            title = Consts.Logger.codeTitle
                            highlightSection(initCursor.position(), parser.reader.currentPosition() - 1)
                        }
                        addSourceCode(fullText, null) {
                            title = Consts.Logger.hintTitle
                            highlightCursorAt(parser.reader.currentPosition())
                            message =
                                    "Try adding an empty property-style block here '${PropertyStyleObjectBlockNode.startToken}${PropertyStyleObjectBlockNode.endToken}'"
                        }
                    }

            return parser.finalizeNode(result, initCursor)
        }
    }
}
