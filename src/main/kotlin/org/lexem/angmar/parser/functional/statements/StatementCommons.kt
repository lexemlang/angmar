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
    fun parseAnyStatement(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            BlockStmtNode.parse(parser, parent, parentSignal) ?: ConditionalStmtNode.parse(parser, parent, parentSignal)
            ?: SelectiveStmtNode.parse(parser, parent, parentSignal) ?: ConditionalLoopStmtNode.parse(parser, parent,
                    parentSignal) ?: IteratorLoopStmtNode.parse(parser, parent, parentSignal)
            ?: InfiniteLoopStmtNode.parse(parser, parent, parentSignal) ?: parseAnyPublicMacroStatement(parser, parent,
                    parentSignal) ?: PublicMacroStmtNode.parse(parser, parent, parentSignal)
            ?: parseAnyControlStatement(parser, parent, parentSignal) ?: FunctionalExpressionStmtNode.parse(parser,
                    parent, parentSignal)

    /**
     * Parses a descriptive statement.
     */
    fun parseAnyDescriptiveStatement(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            LexemePatternGroupNode.parse(parser, parent, parentSignal) ?: LexemePatternNode.parse(parser, parent,
                    parentSignal) ?: OnBackBlockStmtNode.parse(parser, parent, parentSignal)
            ?: QuantifiedLoopStmtNode.parse(parser, parent, parentSignal) ?: SetPropsMacroStmtNode.parse(parser, parent,
                    parentSignal) ?: parseAnyStatement(parser, parent, parentSignal)

    /**
     * Parses a valid statement for the public macro.
     */
    fun parseAnyPublicMacroStatement(parser: LexemParser, parent: ParserNode, parentSignal: Int) =
            VarDeclarationStmtNode.parse(parser, parent, parentSignal) ?: FunctionStmtNode.parse(parser, parent,
                    parentSignal) ?: ExpressionStmtNode.parse(parser, parent, parentSignal) ?: FilterStmtNode.parse(
                    parser, parent, parentSignal)

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
