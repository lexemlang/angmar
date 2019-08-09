package org.lexem.angmar.errors

/**
 * The exception types for the parser.
 */
enum class AngmarParserExceptionType {
    // COMMONS ----------------------------------------------------------------

    MultilineCommentWithoutEndToken,

    EscapedExpressionWithoutExpression,
    EscapedExpressionWithoutEndToken,
    EscapeWithoutCharacter,
    PointEscapeWithoutValue,
    PointEscapeWithFewerDigitsThanAllowed,
    PointEscapeWithBracketsWithoutValue,
    PointEscapeWithBracketsWithMoreDigitsThanAllowed,
    PointEscapeWithBracketsWithoutEndToken,
    QuotedIdentifiersEmpty,
    QuotedIdentifiersWithoutEndQuote,
    QuotedIdentifiersMultilineNotAllowed,
    UnicodeEscapeWithoutValue,
    UnicodeEscapeWithFewerDigitsThanAllowed,
    UnicodeEscapeWithBracketsWithoutValue,
    UnicodeEscapeWithBracketsWithMoreDigitsThanAllowed,
    UnicodeEscapeWithBracketsWithoutEndToken,


    // LITERALS ---------------------------------------------------------------

    BitlistWithoutEndToken,
    FunctionArgumentListWithoutIdentifierAfterSpreadOperator,
    FunctionArgumentListWithoutEndToken,
    FunctionArgumentWithoutExpressionAfterAssignOperator,
    FunctionWithoutBlock,
    IntervalElementWithoutElementAfterRangeOperator,
    IntervalWithoutStartToken,
    IntervalWithoutEndToken,
    IntervalSubIntervalWithoutEndToken,
    ListWithoutEndToken,
    MapElementWithoutRelationalSeparatorAfterKey,
    MapElementWithoutExpressionAfterRelationalSeparator,
    MapWithoutStartToken,
    MapWithoutEndToken,
    NumberWithoutDigitAfterPrefix,
    NumberWithoutDigitAfterDecimalSeparator,
    NumberWithoutDigitAfterExponentSeparator,
    NumberIntegerWithoutDigitAfterPrefix,
    NumberWithSequenceStartedWithADigitSeparator,
    NumberWithSequenceEndedWithADigitSeparator,
    ObjectElementWithoutKeyAfterConstantToken,
    ObjectElementWithoutRelationalOperatorAfterKey,
    ObjectElementWithoutExpressionAfterRelationalOperator,
    ObjectWithoutEndToken,
    ObjectSimplificationWithoutBlock,
    PropertyStyleObjectBlockWithoutEndToken,
    PropertyStyleObjectElementWithoutExpressionAfterName,
    PropertyStyleObjectWithoutStartToken,
    SetWithoutStartToken,
    StringWithoutEndQuote,
    UnescapedStringWithoutEndQuote,
    UnicodeIntervalAbbreviationWithoutEndToken,
    UnicodeIntervalElementWithoutElementAfterRangeOperator,
    UnicodeIntervalWithoutStartToken,
    UnicodeIntervalSubIntervalWithoutEndToken,


    // FUNCTIONAL > EXPRESSIONS > BINARY --------------------------------------

    AdditiveExpressionWithoutExpressionAfterOperator,
    ConditionalExpressionWithoutExpressionAfterOperator,
    LogicalExpressionWithoutExpressionAfterOperator,
    MultiplicativeExpressionWithoutExpressionAfterOperator,
    RelationalExpressionWithoutExpressionAfterOperator,
    ShiftExpressionWithoutExpressionAfterOperator,


    // FUNCTIONAL > EXPRESSIONS > MACROS --------------------------------------

    MacroCheckPropsWithoutPropertyStyleBlockAfterMacroName,


    // FUNCTIONAL > EXPRESSIONS > MODIFIERS -----------------------------------

    FunctionCallExpressionPropertiesWithoutPropertyStyleBlockAfterRelationalToken,
    FunctionCallMiddleArgumentWithoutExpressionAfterRelationalToken,
    FunctionCallWithoutExpressionAfterSpreadOperator,
    FunctionCallWithoutEndToken,
    IndexerWithoutStartToken,
    IndexerWithoutEndToken,


    // FUNCTIONAL > EXPRESSIONS -----------------------------------------------

    AssignExpressionWithoutExpressionAfterAssignOperator,
    BinaryOperatorFollowedByAnotherOperator,
    PrefixOperatorFollowedByAnotherOperator,
    ParenthesisExpressionWithoutExpression,
    ParenthesisExpressionWithoutEndToken,
    PrefixExpressionWithoutExpressionAfterThePrefixOperator,


    // FUNCTIONAL > STATEMENTS > CONTROL --------------------------------------

    ControlWithExpressionStatementWithoutExpression,


    // FUNCTIONAL > STATEMENTS > LOOPS ----------------------------------------

    ConditionalLoopStatementWithoutCondition,
    ConditionalLoopStatementWithoutBlock,
    InfiniteLoopStatementWithoutBlock,
    IteratorLoopStatementWithoutIdentifier,
    IteratorLoopStatementWithoutRelationalKeyword,
    IteratorLoopStatementWithoutExpressionAfterRelationalKeyword,
    IteratorLoopStatementWithoutBlock,
    LastLoopClauseWithoutBlock,
    ElseLoopClauseWithoutBlock,


    // FUNCTIONAL > STATEMENTS > SELECTIVE ------------------------------------

    ConditionalPatternSelectiveStatementWithoutCondition,
    SelectiveCaseStatementWithoutPatternAfterElementSeparator,
    SelectiveCaseStatementWithoutBlock,
    VarPatternSelectiveStatementWithoutIdentifier,


    // FUNCTIONAL > STATEMENTS ------------------------------------------------

    BlockStatementWithoutEndToken,
    ConditionalStatementWithoutCondition,
    ConditionalStatementWithoutThenBlock,
    ConditionalStatementWithoutElseBlock,
    DestructuringElementStatementWithoutIdentifierAfterAliasToken,
    DestructuringSpreadStatementWithoutIdentifierAfterSpreadOperator,
    DestructuringStatementWithoutEndToken,
    FunctionStatementWithoutBlock,
    PublicMacroStatementWithoutValidStatement,
    SelectiveStatementWithoutStartToken,
    SelectiveStatementWithoutAnyCase,
    SelectiveStatementWithoutEndToken,
    SetPropsMacroStatementWithoutPropertyStyleObject,
    VarDeclarationStatementWithoutIdentifier,
    VarDeclarationStatementWithoutAssignOperator,
    VarDeclarationStatementWithoutExpressionAfterAssignOperator,
}
