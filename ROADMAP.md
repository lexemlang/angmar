# Angmar Roadmap

This is the plan to work towards a future release.

> **NOTE**: this is a "living document" and will be edited over time.

## TODO

- Make an `optimize` function in the ParserNode classes.
  - Intervals
  - Strings
  - Parameters
  - Numbers
  - Create a new ParserNode that just holds a LexemPrimitive and stores it in the stack as Last.
- Put type to the contexts: Functional, Expression, Filter and Root.
- ?? - Allow calling the import function from anywhere or just Root contexts.
  - Better to change import function to statement.
- ?? - Pool of objects. Require control over destroy. The dealloc function must work for this.
  - In inheritance use two methods dealloc and deallocInInheritance to dealloc parents without reuse them in their pools.
  - Add realloc method.
  - Lists
  - Maps
  - Objects
  - Contexts
  - Arguments
  - Primitives:
    - Intervals
    - Strings
    - Parameters
    - Numbers
- Review TODOs
- Test forward buffer.
- Check what is public of the library.

- Possibility of use an integer interval to see what cells are ok and what else not to only iterate at the end
through those. 


- The hidden properties are serializable?
- Protect the indexer to access hidden properties.
