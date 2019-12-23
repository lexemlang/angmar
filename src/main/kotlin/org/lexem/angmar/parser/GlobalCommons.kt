package org.lexem.angmar.parser

import org.lexem.angmar.*
import org.lexem.angmar.errors.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.functional.statements.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*
import org.lexem.angmar.parser.literals.*


/**
 * Generic commons for expressions.
 */
internal object GlobalCommons {
    const val constantToken = "#"
    const val elementSeparator = ","
    const val relationalToken = ":"
    const val spreadOperator = ".."
    const val notToken = "!"
    const val wildcardVariable = "_"
    const val tagPrefix = "'"
    val keywords = listOf(NilNode.nilLiteral, LogicNode.trueLiteral, LogicNode.falseLiteral, FunctionNode.keyword,
            ExpressionStmtNode.keyword, FilterStmtNode.keyword, ConditionalStmtNode.ifKeyword,
            ConditionalStmtNode.unlessKeyword, ConditionalStmtNode.elseKeyword, ConditionalLoopStmtNode.whileKeyword,
            ConditionalLoopStmtNode.untilKeyword, InfiniteLoopStmtNode.keyword, IteratorLoopStmtNode.keyword,
            LoopClausesStmtNode.elseKeyword, LoopClausesStmtNode.lastKeyword, SelectiveStmtNode.keyword,
            VarDeclarationStmtNode.variableKeyword, VarDeclarationStmtNode.constKeyword,
            ControlWithExpressionStmtNode.returnKeyword, ControlWithoutExpressionStmtNode.exitKeyword,
            ControlWithoutExpressionStmtNode.nextKeyword, ControlWithoutExpressionStmtNode.redoKeyword,
            ControlWithoutExpressionStmtNode.restartKeyword)

    /**
     * Parses a block statement depending on the context.
     */
    fun parseBlockStatement(parser: LexemParser, parent: ParserNode) = if (parser.isDescriptiveCode) {
        if (parser.isFilterCode) {
            // Descriptive code (filters)
            StatementCommons.parseAnyDescriptiveStatement(parser, parent)
        } else {
            // Descriptive code (expressions)
            StatementCommons.parseAnyDescriptiveStatement(parser, parent)
        }
    } else {
        // Functional code
        StatementCommons.parseAnyStatement(parser, parent)
    }

    /**
     * Parses a lexeme depending on the context.
     */
    fun parseLexem(parser: LexemParser, parent: ParserNode) = if (parser.isDescriptiveCode) {
        if (parser.isFilterCode) {
            // Descriptive code (filters)
            AnyFilterLexemeNode.parse(parser, parent)
        } else {
            // Descriptive code (expressions)
            AnyLexemeNode.parse(parser, parent)
        }
    } else {
        throw AngmarUnreachableException()
    }
}
