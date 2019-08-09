package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.expressions.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*


/**
 * Generic commons for statements.
 */
object StatementCommons {
    /**
     * Parses a statement.
     */
    fun parseAnyStatement(parser: LexemParser) =
            GlobalCommons.parseBlock(parser) ?: ConditionalStmtNode.parse(parser) ?: SelectiveStmtNode.parse(parser)
            ?: ConditionalLoopStmtNode.parse(parser) ?: IteratorLoopStmtNode.parse(parser)
            ?: InfiniteLoopStmtNode.parse(parser) ?: parseAnyPublicMacroStatement(parser) ?: parseAnyMacroStatement(
                    parser) ?: parseAnyControlStatement(parser) ?: ExpressionsCommons.parseExpression(parser)

    /**
     * Parses a valid statement for the public macro.
     */
    fun parseAnyPublicMacroStatement(parser: LexemParser) =
            VarDeclarationStmtNode.parse(parser) ?: FunctionStmtNode.parse(
                    parser) //  TODO add expression statement and filter statement

    /**
     * Parses a macro statement.
     */
    fun parseAnyMacroStatement(parser: LexemParser) =
            PublicMacroStmtNode.parse(parser) ?: SetPropsMacroStmtNode.parse(parser)

    /**
     * Parses a control statement.
     */
    fun parseAnyControlStatement(parser: LexemParser) =
            ControlWithExpressionStmtNode.parse(parser, ControlWithExpressionStmtNode.exitKeyword)
                    ?: ControlWithExpressionStmtNode.parse(parser, ControlWithExpressionStmtNode.returnKeyword,
                            captureTag = false) ?: ControlWithoutExpressionStmtNode.parse(parser,
                            ControlWithoutExpressionStmtNode.nextKeyword) ?: ControlWithoutExpressionStmtNode.parse(
                            parser, ControlWithoutExpressionStmtNode.redoKeyword)
                    ?: ControlWithoutExpressionStmtNode.parse(parser, ControlWithoutExpressionStmtNode.restartKeyword)
}
