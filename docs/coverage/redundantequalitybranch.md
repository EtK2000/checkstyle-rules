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
| Branches use a third operand (e.g. `r = c;`) | n/a | No (check doesn't fire) |
| Operands or branch values impure (method calls, increments) | n/a | No (check doesn't fire) |

## Not supported

The fixer reuses the check's AST classification, so it collapses every shape the
check fires on, with one exception: a comment in the collapse span it cannot
preserve, where it returns a `SkipResult` and leaves the violation for the
developer. (A lone comment that starts and ends on the if-line is relocated onto
the collapsed statement; anything else refuses.) The remaining rows describe
shapes the check never fires on, so no fix is attempted.

| Pattern | Reason |
| --- | --- |
| A comment on any line of the if-else other than the if-line, code following the comment on the if-line (e.g. an inline then-body after a block comment), more than one comment across the span, or a block comment spanning lines | `cannot collapse: a comment in the if-else would be lost` (SkipResult) — the collapse discards those lines and no single relocation target is unambiguous |
| Non-equality condition (`if (a > b)`) or non-if line | The condition must be `==` / `!=`; the check doesn't fire otherwise |
| If-line is the last line of the method body | No body line follows the if |
| Then-line is neither an assignment nor a return (e.g. `throw new ...;`) | The then-branch is neither an assignment nor a return |
| Assign form: the if-else is truncated / incomplete | Invalid Java — the fixer's parse fails (bare `null`); the check does not classify it |
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

When the optional `final T r;` decl and `return r;` trailing return aren't present
(or don't match), the fixer collapses just the if-else and leaves the decl/return
for the developer:

| Pattern | Replacement |
| --- | --- |
| Decl line above the if doesn't match `final T r;` shape | Bare-collapse `r = b;`; decl/return untouched |
| Trailing line below the else doesn't match `return r;` | Same fall-through |
| Decl line variable name differs from the then-target | Same fall-through |
| Trailing return variable differs from the then-target | Same fall-through |

Part of [auto-fix coverage](../auto-fix-coverage.md).