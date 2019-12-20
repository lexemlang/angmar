# Angmar Roadmap

This is the plan to work towards a future release.

> **NOTE**: this is a "living document" and will be edited over time.

## TODO

- Optimize stack. Use a model like objects with differential copies of the levels.
  - ShiftLevel
  - RemoveFromStack
- Optimize the node reference to their parent. Nodes need to be ordered and must be queryable by position and reference.
  - addToParent
- Make an `optimize` function in the ParserNode classes.
  - Intervals
  - Strings
  - Parameters
  - Numbers
  - Create a new ParserNode that just holds a LexemPrimitive and stores it in the stack as Last.
- Review TODOs
- Async spatial garbage collector.
  - Mark objects to remove when they reach 0 references.
  - Keep executing it in all bigNodes until they are clean and frozen. Always try the previous first.
  - Ignore those bigNodes that are marked to be collapsed because the temporal GC will execute it.
- Async temporal garbage collector.
  - Collapse all contiguous bigNodes that are already alive.
  - The object probably will need an index instead of a bigNode reference to avoid problems. If the index is greater than the
    current bigNode, it is ok.
- Check what is public of the library.


- The hidden properties are serializable?
- Protect the indexer to access hidden properties.
