# Angmar Roadmap

This is the plan to work towards a future release.

> **NOTE**: this is a "living document" and will be edited over time.

## TODO

---------------
- Test:
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

- Optimize the node reference using a double linked list of children.
  - Add left and right properties to children.
  - Add size, firstChild and lastChild to parent.
- Make the spatial garbage collector async.
  - Mark objects as garbage when they reach 0 references.
  - Execute over the first frozen node and move forward marking them as clean.
  - Ignore the nodes that are marked to be collapsed.
- Make the temporal garbage collector async.
  - Collapse all contiguous bigNodes that are already alive.
  - The object probably will need an index instead of a bigNode reference to avoid problems when they are moved backwards.
    - Use an incremental identifier for bigNodes so always newer nodes are greater than older ones.
    - If the index is greater than the current bigNode, it is ok.
- Check what is public of the library.
- Check whether the hidden properties are serializable with a test.
- Protect the indexer to access hidden properties.



## Temporal memory model

- No differential.
- Incremental copy of the memory copying the map reference.
  - If there's a writing access, shallow-copies the map and clones the cell and object inside.
  - If the cell is modified but the object not, only clones the cell. For example during a reference change.
  
- Functions must be primitives.
