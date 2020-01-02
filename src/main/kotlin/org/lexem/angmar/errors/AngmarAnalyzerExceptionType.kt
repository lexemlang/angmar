package org.lexem.angmar.errors

/**
 * The exception types for the analyzer.
 * TODO remove not used
 */
enum class AngmarAnalyzerExceptionType {
    // GENERICS ---------------------------------------------------------------

    IncompatibleType,
    IndexOutOfBounds,
    UndefinedIndexer,
    AssignToConstant,
    UnhandledControlStatementSignal,
    CustomError,
    ToStringMethodNotReturningString,

    // IMPORTS ----------------------------------------------------------------

    FileNotExist,
    FileIsNotLexem,

    // MEMORY -----------------------------------------------------------------

    StackNotFoundElement,
    ValueShiftOverSameBigNode,
    CloneOverTheSameBigNode,
    DifferentBigNodeLink,

    HeapSegmentationFault,
    HeapBigNodeLinkFault,
    CannotFreeAReferencedHeapCell,
    ReferenceCountUnderflow,

    FirstBigNodeRollback,

    // INTERVALS --------------------------------------------------------------

    IncorrectRangeBounds,

    // QUANTIFIERS ------------------------------------------------------------

    IncorrectQuantifierBounds,

    // OBJECTS ----------------------------------------------------------------

    CannotModifyAnImmutableView,

    CannotModifyAConstantList,
    CannotModifyAConstantObject,
    CannotModifyAConstantObjectProperty,
    UndefinedObjectProperty,

    CannotModifyAConstantMap,

    CannotModifyAConstantSet,

    IncorrectNodeReference,
    IncorrectNodeChildCount,
    IncorrectNodeWithParent,

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
