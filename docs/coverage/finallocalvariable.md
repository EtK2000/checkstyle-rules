# FinalLocalVariableCheck auto-fix coverage

The fixer inserts `final` before the declaration's type, including for split declarations where
the type and name sit on different lines, and for a declaration that shares a line with a preceding
statement (the `final` lands before the declaration's own type, after the preceding statement's
`;`/`{`/`}`, not at the start of the line).

## Supported (auto-fixable)

| Pattern | Replacement |
| --- | --- |
| Single-line decl (`int x = 5;`) | `final int x = 5;` |
| Multi-var single-line (`int x, y;`) | `final int x, y;` (first fix inserts; the second violation hits the no-op) |
| Split decl, type on its own line (`List<String>` / `l = x;`) | `final` inserted on the type line |
| Split decl with blank line(s) between type and name | walk-back skips blanks, inserts on the type line |
| Decl preceded by another statement on the same line (`X = a; int y = 0;`) | `final` inserted before the declaration's own type (`X = a; final int y = 0;`), locating the type via the nearest preceding `;`/`{`/`}` boundary in the comment/string-masked line |

## Not supported

| Pattern | Skip reason |
| --- | --- |
| Already `final` (single-line, or the split type line) | `declaration is already final` |
| Split multi-var continuation (the line above the name's code ends in a top-level `,`, ignoring any trailing comment or literal contents) | `multi-variable declaration` (`final` applies to the whole declaration, so a non-first variable can't be made final on its own) |
| Split decl whose walk-back lands on a line not starting with a type/modifier/`@` | `no declaration type line precedes the variable name` (e.g. a comment-only line; `final` only legally precedes a type, modifier, or annotation) |
| Decl line was collapsed by a sibling fixer (e.g. `PreferMathMethodFixer` merged `int r;` + branched assign + trailing return into `return Math.max(...);`) | `no declaration type line precedes the variable name` (the original declaration line now starts with a control-flow keyword: `break`/`case`/`catch`/`continue`/`default`/`do`/`else`/`finally`/`for`/`if`/`return`/`switch`/`throw`/`try`/`while`/`yield`, which cannot legally take a `final` prefix) |

Part of [auto-fix coverage](../auto-fix-coverage.md).