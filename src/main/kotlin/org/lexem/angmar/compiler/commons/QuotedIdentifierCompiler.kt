package org.lexem.angmar.compiler.commons

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.commons.*

/**
 * Compiler for [QuotedIdentifierNode].
 */
internal object QuotedIdentifierCompiler {
    fun compile(parent: CompiledNode, parentSignal: Int, node: QuotedIdentifierNode): ConstantCompiled {
        var last = ""
        for ((i, escape) in node.escapes.withIndex()) {
            // Join to the last.
            last += node.texts[i]

            val compiledEscape = escape.compile(parent, parentSignal = -1)

            if (compiledEscape !is ConstantCompiled) {
                throw AngmarUnreachableException()
            }

            val value = compiledEscape.value as LxmInteger

            last += Character.toChars(value.primitive).joinToString("")
        }

        last += node.texts.last()
        return ConstantCompiled(parent, parentSignal, node, LxmString.from(last))
    }
}
