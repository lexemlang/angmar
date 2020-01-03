# Angmar Roadmap

This is the plan to work towards a future release.

> **NOTE**: this is a "living document" and will be edited over time.

## TODO

---------------
- Test:
├── LxmMap
├── LxmObject
├── LxmList
├── LxmSet
├── descriptive
│   ├── LexemePatternContentCompiled.kt
│   ├── lexemes
│   │   ├── BinarySequenceLexemeCompiled.kt
│   │   ├── BlockLexemeCompiled.kt
│   │   ├── GroupLexemeCompiled.kt
│   │   ├── QuantifierLexemeCompiled.kt
│   │   ├── TextLexemeCompiled.kt
├── functional
│   ├── expressions
│   │   ├── PrefixExpressionCompiled.kt
│   │   ├── binary
│   │   │   ├── AdditiveExpressionCompiled.kt
│   │   │   ├── LogicalExpressionCompiled.kt
│   │   │   ├── MultiplicativeExpressionCompiled.kt
│   │   │   └── ShiftExpressionCompiled.kt
│   └── statements
│       ├── DestructuringStmtCompiled.kt
│       ├── loops
│       │   ├── ConditionalLoopStmtCompiled.kt
│       │   ├── InfiniteLoopStmtCompiled.kt
│       │   └── LoopClausesStmtCompiled.kt
│       └── selective
│           ├── ConditionalPatternSelectiveStmtCompiled.kt
│           ├── SelectiveCaseStmtCompiled.kt
├── literals
│   ├── IntervalCompiled.kt
│   ├── IntervalSubIntervalCompiled.kt
│   ├── UnicodeIntervalAbbrCompiled.kt
│   └── UnicodeIntervalSubIntervalCompiled.kt
---------------

- Check what is public of the library.
- Check whether the hidden properties are serializable with a test.
- Protect the indexer to access hidden properties.
- Async garbage collector for the previous BigNodes. No problems. Avoid to continue if it is recovered.
- Async garbage collector for the current one??. No, problems with concurrency during removing.
- Async removal of 0-referenced values??.

17 - 24 - 36 - 40

+1 - +2 - -1 - -2
