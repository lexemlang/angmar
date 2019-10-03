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

    StackUnderflow,
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

    // TEST -------------------------------------------------------------------

    TestControlSignalRaised,
}
