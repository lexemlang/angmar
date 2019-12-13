package org.lexem.angmar.errors

/**
 * The exception types for the parser.
 */
enum class AngmarParserExceptionType {
    // GENERICS ---------------------------------------------------------------

    LexemFileEOFExpected,

    // COMMONS ----------------------------------------------------------------

    MultilineCommentWithoutEndToken,

    EscapedExpressionWithoutExpression,
    EscapedExpressionWithoutEndToken,
    EscapeWithoutCharacter,
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
    FunctionParameterListWithoutIdentifierAfterPositionalSpreadOperator,
    FunctionParameterListWithoutIdentifierAfterNamedSpreadOperator,
    FunctionParameterListWithoutEndToken,
    FunctionParameterWithoutExpressionAfterAssignOperator,
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
    LambdaStatementWithoutExpression,
    ConditionalStatementWithoutCondition,
    ConditionalStatementWithoutThenBlock,
    ConditionalStatementWithoutElseBlock,
    DestructuringElementStatementWithoutIdentifierAfterAliasToken,
    DestructuringSpreadStatementWithoutIdentifierAfterSpreadOperator,
    DestructuringStatementWithoutEndToken,
    FunctionStatementWithoutBlock,
    ExpressionStatementWithoutBlock,
    FilterStatementWithoutBlock,
    PublicMacroStatementWithoutValidStatement,
    SelectiveStatementWithoutStartToken,
    SelectiveStatementWithoutAnyCase,
    SelectiveStatementWithoutEndToken,
    SetPropsMacroStatementWithoutPropertyStyleObject,
    VarDeclarationStatementWithoutIdentifier,
    VarDeclarationStatementWithoutAssignOperator,
    VarDeclarationStatementWithoutExpressionAfterAssignOperator,

    // DESCRIPTIVE > LEXEMES --------------------------------------------------

    QuantifierWithoutMinimumExpression,
    QuantifierWithoutEndToken,
    QuantifierWithoutMaximumValue,
    InlinePropertyPostfixWithoutProperty,
    InlinePropertyPostfixWithBadEnd,
    ExecutorWithoutEndToken,
    ExecutorWithoutExpression,
    ExecutorWithoutExpressionAfterSeparator,
    AccessWithoutNextAccessAfterToken,
    GroupWithoutEndToken,
    GroupWithoutPatterns,
    GroupWithoutLexemeAfterPatternToken,
    QuantifiedGroupPatternWithoutLexemes,
    QuantifiedGroupWithoutEndToken,
    RelativeAnchorGroupWithoutEndToken,
    RelativeAnchorGroupWithoutAnchors,
    AbsoluteAnchorElementWithoutValue,
    AbsoluteAnchorGroupWithoutEndToken,
    AbsoluteAnchorGroupWithoutAnchors,
    FilterLexemeWithoutSelector,
    FilterLexemeWithoutEndToken,
    AdditionFilterLexemeWithoutSelector,
    AdditionFilterLexemeWithoutEndToken,
    AdditionFilterLexemeWithoutNextAccessAfterToken,
    AdditionFilterWithIncorrectSelector,

    // DESCRIPTIVE > SELECTORS ------------------------------------------------

    SelectorMethodWithoutName,
    SelectorMethodArgumentsWithoutEndToken,
    SelectorMethodArgumentsWithoutCondition,
    SelectorPropertyBlockWithoutEndToken,
    SelectorPropertyBlockWithoutCondition,
    SelectorNameGroupWithoutNames,
    SelectorNameGroupWithoutEndToken,
    SelectorPropertyWithoutIdentifier,
    SelectorPropertyGroupWithoutProperties,
    SelectorPropertyGroupWithoutEndToken,

    // DESCRIPTIVE > PATTERNS -------------------------------------------------

    SlaveQuantifiedPatternWithoutMaster,

    // DESCRIPTIVE > STATEMENTS -----------------------------------------------

    OnBackBlockWithoutBlock,

    // DESCRIPTIVE > STATEMENTS > LOOPS ---------------------------------------

    QuantifiedLoopStatementWithoutQuantifier,
    QuantifiedLoopStatementWithoutBlock,
}
