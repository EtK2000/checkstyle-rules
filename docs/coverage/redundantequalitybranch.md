# RedundantEqualityBranchCheck auto-fix coverage

For `==` the surviving value is the else-branch's; for `!=` it's the then-branch's.

| Pattern | Replacement | Auto-fix |
| --- | --- | --- |
| `final int r; if (a == b) r = a; else r = b; return r;` | `return b;` | Yes |
| `if (a == b) r = a; else r = b;` (no decl/return: bare collapse) | `r = b;` | Yes |
| `if (a == b) return a; else return b;` | `return b;` | Yes |
| `if (a == b) return a; return b;` (trailing return) | `return b;` | Yes |
| `if (a != b) ...` (`!=` instead of `==`) | uses then-value | Yes |
| Braced branches (`if (a == b) { r = a; } else { r = b; }`), one or both sides | `r = b;` | Yes |
| Braced decl-collapse (`final int r; if (a == b) { r = a; } else { r = b; } return r;`) | `return b;` | Yes |
| Multi-line condition (`if (a\n== b) ...`) | collapses to the surviving statement | Yes |
| Non-identifier assign target (`arr[i]`, `this.x`) | `arr[i] = b;` | Yes |
| If-line trailing line comment (`if (a == b) // note`) | `r = b; // note` (comment relocated) | Yes |
| If-line trailing block comment (`if (a == b) /* note */`) | `r = b; /* note */` (comment relocated) | Yes |
| The collapse is the body of code ahead of it on its first line (`else if (a == b)`, `} else if (a == b) {`, `out: if (a == b)`, `if (c) if (a == b)`, `do if (a == b) r = a; else r = b; while (r < 0);`, `void m(int a, int b) { if (a == b) r = a; else r = b; }`, `int m(int a, int b) { final int r;` above the decl-collapse) | that code kept on a line of its own, the collapsed statement one level in below it (`else` / `r = b;`) | Yes |
| A statement ahead of the collapse on its first line (`r = 1; if (a == b)`, or a text block's closing `""";`) | that statement kept on a line of its own, the collapsed statement below it at that statement's indent, not the line's (`"""` closers sit deeper); a comment on the if-line is found after the text block, not taken as text | Yes |
| Code after the collapse on its last line (`else r = b; }`, `return r; }`, `if (a == b) r = a; else r = b; ++count; // note`) | kept after the collapsed statement (`r = b; }`, `r = b; ++count; // note`), ahead of a relocated if-line comment (`r = b; } // note`) | Yes |
| Branches use a third operand (e.g. `r = c;`) | n/a | No (check doesn't fire) |
| Operands or branch values impure (method calls, increments) | n/a | No (check doesn't fire) |

## Not supported

The fixer collapses every shape the check fires on, with one exception: a comment in the collapse span it cannot
preserve, where it skips the fix and leaves the violation for the
developer. (A lone comment that starts and ends on the if-line is relocated onto
the collapsed statement; anything else refuses.) The remaining rows describe
shapes the check never fires on, so no fix is attempted.

| Pattern | Reason |
| --- | --- |
| A comment on any line of the if-else other than the if-line, code following the comment on the if-line (e.g. an inline then-body after a block comment, or the `if` itself after one: `/* note */ if (a == b)`), more than one comment across the span, or a block comment spanning lines | Skipped (`cannot collapse the if-else: a comment in it would be lost`): the collapse discards those lines and no single relocation target is unambiguous |
| Non-equality condition (`if (a > b)`) or non-if line | The condition must be `==` / `!=`; the check doesn't fire otherwise |
| If-line is the last line of the method body | No body line follows the if |
| Then-line is neither an assignment nor a return (e.g. `throw new ...;`) | The then-branch is neither an assignment nor a return |
| Assign form: the if-else is truncated / incomplete | Invalid Java, so the check does not classify it |
| Assign form: no `else` line directly below | No `else` line where the assign form expects it |
| Assign form: else body is not an assignment (e.g. `throw new ...;`) | The else-branch is not an assignment |
| Then/else assign targets differ (`r = a;` then `s = b;`) | The then and else branches assign to different targets |
| A branch value is neither equality operand (e.g. `r = c;`) | One branch value isn't `a` or `b` |
| Return form (if-else): else body is not a return statement | The else-branch is not a return statement |
| Return form (if-else): a returned value is neither equality operand | A returned value isn't `a` or `b` |
| Return form (trailing): line after the if is not a return statement | The line after the if is not a return statement |
| Return form (trailing): a returned value is neither equality operand | A returned value isn't `a` or `b` |
| Return form: no else and no trailing return below | No `else` and no trailing return after the if |

## Degrades to bare collapse (fixer fixes, but to a less-collapsed form)

When the optional uninitialized `T r;` decl (`final` or not) and `return r;` trailing return aren't present
(or don't match), the fixer collapses just the if-else and leaves the decl/return
for the developer:

| Pattern | Replacement |
| --- | --- |
| Statement above the if isn't an uninitialized `T r;` decl (e.g. `int r = 0;`, or no decl) | Bare-collapse `r = b;`; decl/return untouched |
| Trailing line below the else doesn't match `return r;` | Same fall-through |
| Decl line variable name differs from the then-target | Same fall-through |
| Trailing return variable differs from the then-target | Same fall-through |
| The declaration names another variable too (`int s = compute(), r;`, `int r, s;` with `s` the target) | Same fall-through: deleting the statement would drop the other name and its initializer |
| The declaration sits straight in an old-style `case` group (`case 1: int r; ...`) | Same fall-through: the later cases share its scope and may use the variable (`case 2: r = 5;`). A `case 1 -> { ... }` block is a scope of its own and collapses |
| The declaration carries an annotation (`@SuppressWarnings("unused") final int r;`) | Same fall-through: deleting it would drop the annotation |
| The declared type is not the enclosing method's return type (`double r;` in a method returning `Object`), or the code sits in a lambda, constructor or initializer | Same fall-through: returning the value directly could convert it differently than assigning it did (`Integer` instead of `Double`) |

Part of [auto-fix coverage](../auto-fix-coverage.md).