package org.lexem.angmar.compiler.commons

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.commons.*

/**
 * Compiler for [EscapeNode].
 */
internal object EscapeCompiler {
    fun compile(parent: CompiledNode, parentSignal: Int, node: EscapeNode): ConstantCompiled {
        val char = when (node.value) {
            "t" -> '\t'.toInt()
            "n" -> '\n'.toInt()
            "r" -> '\r'.toInt()
            else -> node.value[0].toInt()
        }

        return ConstantCompiled(parent, parentSignal, node, LxmInteger.from(char))
    }
}
