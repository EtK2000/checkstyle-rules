# RedundantCastCheck auto-fix coverage

Deletes `(Type)` plus trailing whitespace at the violation column, preserving any inline block comment between the cast's `)` and the expression. Also strips surrounding parens in two cases: receiver wrap (`((Foo) x).y` -> `x.y`) and bare-cast wrap (`return ((Foo) x);` -> `return x;`), inserting a space after a `return`/`throw`/`yield` keyword when needed.

## Not supported

| Pattern | Reason |
| --- | --- |
| Multi-line cast, or a cast with no expression on the same line | The cast span can't be resolved on a single line |

Part of [auto-fix coverage](../auto-fix-coverage.md).