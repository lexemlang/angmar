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
    CustomError,
    ToStringMethodNotReturningString,

    // IMPORTS ----------------------------------------------------------------

    FileNotExist,
    FileIsNotLexem,

    // MEMORY -----------------------------------------------------------------

    StackNotFoundElement,

    HeapSegmentationFault,
    HeapBigNodeLinkFault,
    ReferencedHeapCellFreed,
    ReferenceCountUnderflow,

    FirstBigNodeRollback,
    NonRecoverableNodeRollback,

    LastBigNodeTemporalGarbageCollection,
    CannotReachLastBigNodeInTemporalGarbageCollectionGroup,

    // NUMBERS ----------------------------------------------------------------

    NumberOverflow,

    // INTERVALS --------------------------------------------------------------

    IncorrectRangeBounds,

    // QUANTIFIERS ------------------------------------------------------------

    IncorrectQuantifierBounds,

    // OBJECTS ----------------------------------------------------------------

    CannotModifyAnImmutableView,

    CannotModifyAConstantList,
    CannotModifyANonWritableList,

    CannotModifyAConstantObject,
    CannotModifyANonWritableObject,
    CannotModifyAConstantObjectProperty,
    UndefinedObjectProperty,

    CannotModifyAConstantMap,

    CannotModifyAConstantSet,

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
