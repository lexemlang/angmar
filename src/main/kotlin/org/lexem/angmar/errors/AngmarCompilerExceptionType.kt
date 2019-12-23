package org.lexem.angmar.errors

/**
 * The exception types for the compiler.
 */
enum class AngmarCompilerExceptionType {
    // GENERICS ---------------------------------------------------------------

    InfiniteLoop,
    IncompatibleType,

    // NUMBERS ----------------------------------------------------------------

    NumberOverflow,

    // INTERVALS --------------------------------------------------------------

    IncorrectRangeBounds,

    // QUANTIFIERS ------------------------------------------------------------

    IncorrectQuantifierBounds,
}
