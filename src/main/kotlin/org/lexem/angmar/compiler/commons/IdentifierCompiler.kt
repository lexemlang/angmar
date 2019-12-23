package org.lexem.angmar.compiler.commons

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.compiler.*
import org.lexem.angmar.compiler.others.*
import org.lexem.angmar.parser.commons.*

/**
 * Compiler for [IdentifierNode].
 */
internal object IdentifierCompiler {
    fun compile(parent: CompiledNode, parentSignal: Int, node: IdentifierNode): CompiledNode {
        if (node.isQuotedIdentifier) {
            return node.quotedIdentifier!!.compile(parent, parentSignal).changeParserNode(node)
        }

        val value = node.simpleIdentifiers.joinToString(IdentifierNode.middleChar)
        return ConstantCompiled(parent, parentSignal, node, LxmString.from(value))
    }
}
