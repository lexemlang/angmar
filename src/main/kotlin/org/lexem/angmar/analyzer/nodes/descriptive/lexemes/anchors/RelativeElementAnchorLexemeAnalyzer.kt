package org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.io.*
import org.lexem.angmar.parser.commons.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*
import org.lexem.angmar.utils.*


/**
 * Analyzer for relative element of anchor lexemes.
 */
internal object RelativeElementAnchorLexemeAnalyzer {
    const val signalEndValue = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: RelativeElementAnchorLexemeNode) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                val isStart = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.AnchorIsStart) as LxmLogic
                val isStartLogic = isStart.primitive
                val isForward = analyzer.isForward()

                when (node.type) {
                    RelativeElementAnchorLexemeNode.RelativeAnchorType.Text -> {
                        if (isStartLogic xor isForward) {
                            if (!analyzer.text.isEnd()) {
                                analyzer.memory.addToStackAsLast(LxmLogic.False)
                            } else {
                                analyzer.memory.addToStackAsLast(LxmLogic.True)
                            }
                        } else {
                            if (!analyzer.text.isStart()) {
                                analyzer.memory.addToStackAsLast(LxmLogic.False)
                            } else {
                                analyzer.memory.addToStackAsLast(LxmLogic.True)
                            }
                        }
                    }
                    RelativeElementAnchorLexemeNode.RelativeAnchorType.Line -> {
                        val reader = analyzer.text as? ITextReader ?: throw AngmarException(
                                "The relative anchor line type require that the analyzed content is a text reader.")

                        if (isForward xor isStartLogic) {
                            if (!reader.isEnd() && reader.currentChar() !in WhitespaceNode.endOfLineChars) {
                                analyzer.memory.addToStackAsLast(LxmLogic.False)
                            } else {
                                analyzer.memory.addToStackAsLast(LxmLogic.True)
                            }
                        } else {
                            if (!reader.isStart() && reader.prevChar() !in WhitespaceNode.endOfLineChars) {
                                analyzer.memory.addToStackAsLast(LxmLogic.False)
                            } else {
                                analyzer.memory.addToStackAsLast(LxmLogic.True)
                            }
                        }
                    }
                    RelativeElementAnchorLexemeNode.RelativeAnchorType.StringValue -> {
                        return analyzer.nextNode(node.value)
                    }
                    RelativeElementAnchorLexemeNode.RelativeAnchorType.IntervalValue -> {
                        return analyzer.nextNode(node.value)
                    }
                }
            }
            signalEndValue -> {
                val isStart = analyzer.memory.getFromStack(AnalyzerCommons.Identifiers.AnchorIsStart) as LxmLogic
                val isStartLogic = isStart.primitive
                val value = analyzer.memory.getLastFromStack()

                when (node.type) {
                    RelativeElementAnchorLexemeNode.RelativeAnchorType.StringValue -> {
                        val reader = analyzer.text as? ITextReader ?: throw AngmarException(
                                "The relative anchor string type require that the analyzed content is a text reader.")
                        value as LxmString

                        var text = value.primitive
                        val cursor = reader.saveCursor()
                        val isForward = !(analyzer.isForward() xor isStartLogic)
                        val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)
                        val insensible = RelationalFunctions.isTruthy(
                                props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Insensible)
                                        ?: LxmNil)

                        if (!isForward) {
                            reader.back()
                            text = text.reversed()
                        }

                        if (insensible) {
                            text = text.toUnicodeLowercase()
                        }

                        for (ch in text) {
                            var chText = reader.currentChar()

                            if (insensible) {
                                chText = chText?.toUnicodeLowercase()
                            }

                            if (chText == null || chText != ch) {
                                analyzer.memory.replaceLastStackCell(LxmLogic.False)

                                cursor.restore()

                                return analyzer.nextNode(node.parent, node.parentSignal)
                            }

                            if (isForward) {
                                reader.advance()
                            } else {
                                reader.back()
                            }
                        }

                        analyzer.memory.replaceLastStackCell(LxmLogic.True)

                        cursor.restore()
                    }
                    RelativeElementAnchorLexemeNode.RelativeAnchorType.IntervalValue -> {
                        val reader = analyzer.text as? ITextReader ?: throw AngmarException(
                                "The relative anchor interval type require that the analyzed content is a text reader.")
                        value as LxmInterval

                        val interval = value.primitive
                        val cursor = reader.saveCursor()
                        val isForward = !(analyzer.isForward() xor isStartLogic)
                        val props = AnalyzerCommons.getCurrentNodeProps(analyzer.memory)
                        val insensible = RelationalFunctions.isTruthy(
                                props.getPropertyValue(analyzer.memory, AnalyzerCommons.Properties.Insensible)
                                        ?: LxmNil)

                        if (!isForward) {
                            reader.back()
                        }

                        if (insensible) {
                            val chText = reader.currentChar()

                            if (chText == null || (!interval.contains(
                                            chText.toUnicodeLowercase().toInt()) && !interval.contains(
                                            chText.toUnicodeUppercase().toInt()))) {
                                analyzer.memory.replaceLastStackCell(LxmLogic.False)
                            } else {
                                analyzer.memory.replaceLastStackCell(LxmLogic.True)
                            }
                        } else {
                            val chText = reader.currentChar()

                            if (chText == null || !interval.contains(chText.toInt())) {
                                analyzer.memory.replaceLastStackCell(LxmLogic.False)
                            } else {
                                analyzer.memory.replaceLastStackCell(LxmLogic.True)
                            }
                        }

                        cursor.restore()
                    }
                    else -> throw AngmarUnreachableException()
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
