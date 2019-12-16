# Angmar Roadmap

This is the plan to work towards a future release.

> **NOTE**: this is a "living document" and will be edited over time.

## TODO

- Change collapseTo to just mark the bignodes to be non-recoverable and delegate the real collapsing to another time.
  - When collapsing if there are more than x bignodes, it executes directly the collapsing over that group. Probably 10 or 20
  - During the temporal garbage collector it tracks the non recoverable groups and collapses them.
    - It should collapse the most consuming bignodes, i.e. those groups that have more elements in their heaps.
- Check what is public of the library.
