# Angmar Roadmap

This is the plan to work towards a v1 release. This is a "living document" and will be edited over time.

## Milestones

- [ ] A fully implemented parser for the Lexem language
  - [x] Commons
  - [x] Functional expressions
  - [x] Functional statements
  - [ ] Expressions
  - [ ] Filters
- [ ] A fully implementation of an interpreter
  - [x] Design a memory system that supports backtracking
  - [x] Design a suitable garbage collector
    - [x] Use reference counting + cyclic collector for spacial garbage
    - [x] Use collapsing for temporal garbage

## TODO

- Stdlib
  - All methods
- Parsers
  - Descriptive elements
- Analyzers
  - Expressions
    - MacroCheckProps - `when descriptive is available`
    - MacroBacktrack - `when descriptive is available`
  - Statements
    - SetPropsMacroStmtNode - `when descriptive is available`
    
- E2E tests
