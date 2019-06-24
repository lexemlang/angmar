package org.lexem.angmar.nodes.expressions

import org.lexem.angmar.LexemParser
import org.lexem.angmar.nodes.expressions.modifiers.AccessExpressionNode
import org.lexem.angmar.nodes.literals.BooleanNode
import org.lexem.angmar.nodes.literals.NilNode
import org.lexem.angmar.nodes.literals.NumberNode


/**
 * Commons for expressions.
 */
object ExpressionsCommons {
    /**
     * Parses any of the element modifiers.
     */
    fun parseElementModifier(parser: LexemParser) = AccessExpressionNode.parse(parser)

    /**
     * Parses any literal.
     */
    fun parseLiteral(parser: LexemParser) =
            NilNode.parse(parser) ?: BooleanNode.parse(parser) ?: NumberNode.parseAnyNumberDefaultDecimal(parser)
}