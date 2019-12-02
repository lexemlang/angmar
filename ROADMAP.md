# Angmar Roadmap

This is the plan to work towards a v1 release. This is a "living document" and will be edited over time.

## Milestones

- Make all stdlib methods
- Re-do the grammar
- Do Univer
- Do Parser for (E)BNF and a transpiler to Lexem 
- Do Parser for simple ANTLR and a transpiler to Lexem
- Transpile main grammars to Lexem
- (OPTIONAL) Add Lexicon as a new data type only for text

## TODO

- Stdlib
  - Globals
    - Math
  - Prototypes
    - BitList
    - Float
    - Integer
    - Interval
    - List
    - Map
    - Object
    - Set
    - String

- Test for Unicode
- E2E tests
  - Make a DSL to check the results.
- Allow to create an analyzer from a group of parsers.
- Execute the garbage collector by an threshold that is incremented only if the empty cells are
  at least the 20% of the total.
