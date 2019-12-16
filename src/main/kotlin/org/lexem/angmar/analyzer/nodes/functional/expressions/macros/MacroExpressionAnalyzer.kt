package org.lexem.angmar.analyzer.nodes.functional.expressions.macros

import org.lexem.angmar.*
import org.lexem.angmar.analyzer.*
import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import java.io.*


/**
 * Analyzer for macro expressions.
 */
internal object MacroExpressionAnalyzer {
    fun stateMachine(analyzer: LexemAnalyzer, signal: Int, node: MacroExpressionNode) {
        when (node.macroName) {
            MacroExpressionNode.lineMacro -> {
                val lineColumn = node.from.lineColumn()
                analyzer.memory.addToStackAsLast(LxmInteger.from(lineColumn.first))
            }
            MacroExpressionNode.fileMacro -> {
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                val source = context.getPropertyValue(analyzer.memory, AnalyzerCommons.Identifiers.HiddenFilePath)!!
                analyzer.memory.addToStackAsLast(source)
            }
            MacroExpressionNode.directoryMacro -> {
                val context = AnalyzerCommons.getCurrentContext(analyzer.memory, toWrite = false)
                val source = context.getPropertyValue(analyzer.memory,
                        AnalyzerCommons.Identifiers.HiddenFilePath) as LxmString
                analyzer.memory.addToStackAsLast(LxmString.from(File(source.primitive).parentFile.canonicalPath))
            }
            MacroExpressionNode.fileContentMacro -> {
                val text = node.parser.reader.readAllText()
                analyzer.memory.addToStackAsLast(LxmString.from(text))
            }
        }

        return analyzer.nextNode(node.parent, node.parentSignal)
    }
}
