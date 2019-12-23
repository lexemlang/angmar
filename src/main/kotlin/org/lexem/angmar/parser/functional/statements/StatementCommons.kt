package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.descriptive.*
import org.lexem.angmar.parser.descriptive.statements.*
import org.lexem.angmar.parser.descriptive.statements.loops.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*


/**
 * Generic commons for statements.
 */
internal object StatementCommons {
    /**
     * Parses a statement.
     */
    fun parseAnyStatement(parser: LexemParser, parent: ParserNode) =
            BlockStmtNode.parse(parser, parent) ?: ConditionalStmtNode.parse(parser, parent) ?: SelectiveStmtNode.parse(
                    parser, parent) ?: ConditionalLoopStmtNode.parse(parser, parent) ?: IteratorLoopStmtNode.parse(
                    parser, parent) ?: InfiniteLoopStmtNode.parse(parser, parent) ?: parseAnyPublicMacroStatement(
                    parser, parent) ?: PublicMacroStmtNode.parse(parser, parent) ?: parseAnyControlStatement(parser,
                    parent) ?: FunctionalExpressionStmtNode.parse(parser, parent)

    /**
     * Parses a descriptive statement.
     */
    fun parseAnyDescriptiveStatement(parser: LexemParser, parent: ParserNode) =
            LexemePatternGroupNode.parse(parser, parent) ?: LexemePatternNode.parse(parser, parent)
            ?: OnBackBlockStmtNode.parse(parser, parent) ?: QuantifiedLoopStmtNode.parse(parser, parent)
            ?: SetPropsMacroStmtNode.parse(parser, parent) ?: parseAnyStatement(parser, parent)

    /**
     * Parses a valid statement for the public macro.
     */
    fun parseAnyPublicMacroStatement(parser: LexemParser, parent: ParserNode) =
            VarDeclarationStmtNode.parse(parser, parent) ?: FunctionStmtNode.parse(parser, parent)
            ?: ExpressionStmtNode.parse(parser, parent) ?: FilterStmtNode.parse(parser, parent)

    /**
     * Parses a control statement.
     */
    fun parseAnyControlStatement(parser: LexemParser, parent: ParserNode) =
            ControlWithExpressionStmtNode.parse(parser, parent, ControlWithExpressionStmtNode.returnKeyword,
                    captureTag = false) ?: ControlWithoutExpressionStmtNode.parse(parser, parent,
                    ControlWithoutExpressionStmtNode.exitKeyword) ?: ControlWithoutExpressionStmtNode.parse(parser,
                    parent, ControlWithoutExpressionStmtNode.nextKeyword) ?: ControlWithoutExpressionStmtNode.parse(
                    parser, parent, ControlWithoutExpressionStmtNode.redoKeyword)
            ?: ControlWithoutExpressionStmtNode.parse(parser, parent, ControlWithoutExpressionStmtNode.restartKeyword)
}
