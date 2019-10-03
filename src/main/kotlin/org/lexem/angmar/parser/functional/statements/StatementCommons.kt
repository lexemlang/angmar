package org.lexem.angmar.parser.functional.statements

import org.lexem.angmar.*
import org.lexem.angmar.parser.*
import org.lexem.angmar.parser.functional.statements.controls.*
import org.lexem.angmar.parser.functional.statements.loops.*


/**
 * Generic commons for statements.
 */
internal object StatementCommons {
    /**
     * Parses a statement.
     */
    fun parseAnyStatement(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            GlobalCommons.parseBlock(parser, parent, parentSignal) ?: ConditionalStmtNode.parse(parser, parent,
                    parentSignal) ?: SelectiveStmtNode.parse(parser, parent, parentSignal)
            ?: ConditionalLoopStmtNode.parse(parser, parent, parentSignal) ?: IteratorLoopStmtNode.parse(parser, parent,
                    parentSignal) ?: InfiniteLoopStmtNode.parse(parser, parent, parentSignal)
            ?: parseAnyPublicMacroStatement(parser, parent, parentSignal) ?: PublicMacroStmtNode.parse(parser, parent,
                    parentSignal) ?: parseAnyControlStatement(parser, parent, parentSignal) ?: ExpressionStmtNode.parse(
                    parser, parent, parentSignal)

    /**
     * Parses a valid statement for the public macro.
     */
    fun parseAnyPublicMacroStatement(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            VarDeclarationStmtNode.parse(parser, parent, parentSignal) ?: FunctionStmtNode.parse(parser, parent,
                    parentSignal) //  TODO add expression statement and filter statement

    /**
     * Parses a control statement.
     */
    fun parseAnyControlStatement(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            ControlWithExpressionStmtNode.parse(parser, parent, parentSignal,
                    ControlWithExpressionStmtNode.returnKeyword, captureTag = false)
                    ?: ControlWithoutExpressionStmtNode.parse(parser, parent, parentSignal,
                            ControlWithoutExpressionStmtNode.exitKeyword) ?: ControlWithoutExpressionStmtNode.parse(
                            parser, parent, parentSignal, ControlWithoutExpressionStmtNode.nextKeyword)
                    ?: ControlWithoutExpressionStmtNode.parse(parser, parent, parentSignal,
                            ControlWithoutExpressionStmtNode.redoKeyword) ?: ControlWithoutExpressionStmtNode.parse(
                            parser, parent, parentSignal, ControlWithoutExpressionStmtNode.restartKeyword)
}
