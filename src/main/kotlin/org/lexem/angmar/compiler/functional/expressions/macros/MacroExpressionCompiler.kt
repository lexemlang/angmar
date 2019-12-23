package org.lexem.angmar.compiler.functional.expressions.macros

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.functional.expressions.macros.*
import java.io.*

/**
 * Compiler for [MacroExpressionNode].
 */
internal object MacroExpressionCompiler {
    fun compile(parent: CompiledNode, parentSignal: Int, node: MacroExpressionNode): ConstantCompiled {
        val result = when (node.macroName) {
            MacroExpressionNode.lineMacro -> {
                val lineColumn = node.from.lineColumn()
                LxmInteger.from(lineColumn.first)
            }
            MacroExpressionNode.fileMacro -> {
                LxmString.from(node.parser.reader.getSource())
            }
            MacroExpressionNode.directoryMacro -> {
                val source = node.parser.reader.getSource()
                LxmString.from(File(source).parentFile.canonicalPath)
            }
            MacroExpressionNode.fileContentMacro -> {
                val text = node.parser.reader.readAllText()
                LxmString.from(text)
            }
            else -> throw AngmarUnreachableException()
        }

        return ConstantCompiled(parent, parentSignal, node, result)
    }
}
