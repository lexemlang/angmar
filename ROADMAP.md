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



- Poner en los nodos referencias para hacer una linked-list.
- No añadir un nodo hasta que se termine su expresión o grupo.
- Recolector de basura asíncrono para los nodos anteriores. Se puede modificar el mapa ya que las celdas eliminadas no se van a tocar en el futuro.
- Para el nodo actual recolector asíncrono sin parada. Para ello no debe haber ningún punto en el que se elimine antes de añadir. Nodos??
- Cuando el main thread detecta una referencia a 0. La manda a otro hilo para que recorra sus punteros y reduzca las referencias del resto.
- Asociar el returnCodePoint al bignode para no modificar el heap si no es necesario.
- Hacer un intervalo modificable como un Red-Black tree para el recolector.
