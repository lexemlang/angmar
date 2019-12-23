package org.lexem.angmar.analyzer.nodes.descriptive.lexemes.anchors

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.analyzer.nodes.*
import org.lexem.angmar.analyzer.stdlib.types.*
import org.lexem.angmar.compiler.descriptive.lexemes.anchors.*
import org.lexem.angmar.config.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.lexemes.anchors.*


/**
 * Analyzer for absolute elements of anchor lexemes.
 */
internal object AbsoluteElementAnchorLexemeAnalyzer {
    const val signalEndExpression = AnalyzerNodesCommons.signalStart + 1

    // METHODS ----------------------------------------------------------------

    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: AbsoluteElementAnchorLexemeCompiled) {
        when (signal) {
            AnalyzerNodesCommons.signalStart -> {
                return analyzer.nextNode(node.expression)
            }
            signalEndExpression -> {
                val value = analyzer.memory.getLastFromStack() as? LxmInteger ?: throw AngmarAnalyzerException(
                        AngmarAnalyzerExceptionType.IncompatibleType,
                        "The value of an ${node.type.identifier} absolute anchor type must be an ${IntegerType.TypeName}.") {
                    val fullText = node.parser.reader.readAllText()
                    addSourceCode(fullText, node.parser.reader.getSource()) {
                        title = Consts.Logger.codeTitle
                        highlightSection(node.from.position(), node.to.position() - 1)
                    }
                    addSourceCode(fullText, null) {
                        title = Consts.Logger.hintTitle
                        highlightSection(node.expression.from.position(), node.expression.to.position() - 1)
                        message = "Review the returned value of this expression"
                    }
                }

                when (node.type) {
                    AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromStart -> {
                        if (analyzer.text.currentPosition() != value.primitive) {
                            analyzer.memory.replaceLastStackCell(LxmLogic.False)
                        } else {
                            analyzer.memory.replaceLastStackCell(LxmLogic.True)
                        }
                    }
                    AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.FromEnd -> {
                        val textLength = analyzer.text.getLength()

                        if (analyzer.text.currentPosition() != textLength - value.primitive) {
                            analyzer.memory.replaceLastStackCell(LxmLogic.False)
                        } else {
                            analyzer.memory.replaceLastStackCell(LxmLogic.True)
                        }
                    }
                    AbsoluteElementAnchorLexemeNode.AbsoluteAnchorType.AnalysisBeginning -> {
                        if (analyzer.text.currentPosition() != value.primitive + analyzer.initialCursor.position()) {
                            analyzer.memory.replaceLastStackCell(LxmLogic.False)
                        } else {
                            analyzer.memory.replaceLastStackCell(LxmLogic.True)
                        }
                    }
                }
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
