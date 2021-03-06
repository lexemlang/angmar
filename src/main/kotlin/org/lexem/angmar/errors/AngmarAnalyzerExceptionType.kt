package org.lexem.angmar.errors

/**
 * The exception types for the analyzer.
 */
enum class AngmarAnalyzerExceptionType {
    // GENERICS ---------------------------------------------------------------

    IncompatibleType,
    IndexOutOfBounds,
    UndefinedIndexer,
    AssignToConstant,
    UnhandledControlStatementSignal,

    // IMPORTS ----------------------------------------------------------------

    FileNotExist,
    FileIsNotLexem,

    // MEMORY -----------------------------------------------------------------

    StackNotFoundElement,

    HeapSegmentationFault,
    ReferencedHeapCellFreed,
    ReferenceCountUnderflow,

    FirstBigNodeRollback,
    BigNodeDoesNotBelongToMemoryChain,

    // NUMBERS ----------------------------------------------------------------

    NumberOverflow,

    // INTERVALS --------------------------------------------------------------

    IncorrectRangeBounds,

    // QUANTIFIERS ------------------------------------------------------------

    IncorrectQuantifierBounds,

    // OBJECTS ----------------------------------------------------------------

    CannotModifyAConstantList,

    CannotModifyAConstantObject,
    CannotModifyAConstantObjectProperty,
    UndefinedObjectProperty,

    CannotModifyAConstantMap,

    // FUNCTIONS --------------------------------------------------------------

    BadArgumentError,
    BadThisArgumentTypeError,
    FunctionCallWithoutThisArgument,

    // FILTERS ----------------------------------------------------------------

    FilterCallWithoutNode2FilterArgument,

    // TEST -------------------------------------------------------------------

    TestControlSignalRaised,

    // SELECTORS --------------------------------------------------------------

    UnrecognizedSelectorPropertyAtIdentifier,
    UnrecognizedSelectorMethod,
    IncorrectSelectorMethodArguments,

    // LEXEMES ----------------------------------------------------------------

    AccessLexemWithNextRequiresANode,
    AdditionLexemWithNextRequiresANode,
    FilterLexemWithNextRequiresANode,

    // PATTERNS ---------------------------------------------------------------

    PatternUnionAlreadyExists,
    PatternUnionWithoutQuantifier,
    QuantifierMinimumIsGreaterThanNumberOfPatterns,
}
