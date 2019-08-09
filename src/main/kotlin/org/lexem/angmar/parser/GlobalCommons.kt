package org.lexem.angmar.parser

import org.lexem.angmar.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*
import org.lexem.angmar.parser.literals.*


/**
 * Generic commons for expressions.
 */
object GlobalCommons {
    const val constantToken = "#"
    const val elementSeparator = ","
    const val relationalToken = ":"
    const val spreadOperator = ".."
    const val notToken = "!"
    const val wildcardVariable = "_"
    const val tagPrefix = "'"
    val keywords = listOf(NilNode.nilLiteral, LogicNode.trueLiteral, LogicNode.falseLiteral,
            FunctionNode.keyword /* TODO add exp and filter */, ConditionalStmtNode.ifKeyword,
            ConditionalStmtNode.unlessKeyword, ConditionalStmtNode.elseKeyword, ConditionalLoopStmtNode.whileKeyword,
            ConditionalLoopStmtNode.untilKeyword, InfiniteLoopStmtNode.keyword, IteratorLoopStmtNode.keyword,
            LoopClausesStmtNode.elseKeyword, LoopClausesStmtNode.lastKeyword, SelectiveStmtNode.keyword,
            VarDeclarationStmtNode.variableKeyword, VarDeclarationStmtNode.constKeyword,
            ControlWithExpressionStmtNode.exitKeyword, ControlWithExpressionStmtNode.returnKeyword,
            ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword,
            ControlWithoutExpressionStmtNode.restartKeyword)


    /**
     * Parses a block depending on the context.
     */
    fun parseBlock(parser: LexemParser) = if (parser.isDescriptiveCode) {
        if (parser.isFilterCode) {
            // Descriptive code (filters)
            // TODO replace with block
            NumberNode.parseAnyNumberDefaultDecimal(parser)
        } else {
            // Descriptive code (expressions)
            // TODO replace with block
            NumberNode.parseAnyNumberDefaultDecimal(parser)
        }
    } else {
        // Functional code
        BlockStmtNode.parse(parser)
    }
}
