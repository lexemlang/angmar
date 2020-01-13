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

- Whenever a section of descriptive code does not depend on functional code, execute it directly.
  - Cannot have functional code or an access lexeme.
  
  
Rollback to n -> remove those
Collapse to n -> get the maximum of those

Map instead of set. The map holds bigNode ids as keys and the next bigNode that belongs to them.


- During backtracking it must remove all future ids.
- During getCell gets the last less or equal than the id (like now).
- During garbage collection remove those cells that don't belong to the aliveBigNodes set, and those that are inside
  leave them if there is no greater cell between itself and the maximum.


The collapsed are alive.
When rollback you must remove all previous.
