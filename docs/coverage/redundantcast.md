# RedundantCastCheck auto-fix coverage

Deletes `(Type)` plus trailing whitespace at the violation column, preserving any inline block comment between the cast's `)` and the expression. Also strips surrounding parens in two cases: receiver wrap (`((Foo) x).y` -> `x.y`) and bare-cast wrap (`return ((Foo) x);` -> `return x;`), inserting a space after a `return`/`throw`/`yield` keyword when needed.

## Not supported

| Pattern | Reason |
| --- | --- |
| Multi-line cast (the cast's `(` and `)` on different lines) | The cast span can't be resolved on a single line |
| Cast with no expression after it on the violation line | The expression the cast applies to is not on that line |

Part of [auto-fix coverage](../auto-fix-coverage.md).