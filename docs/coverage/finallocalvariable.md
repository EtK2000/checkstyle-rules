# FinalLocalVariableCheck auto-fix coverage

The fixer inserts `final` at the start of the declaration, before its type or any leading annotation
(`@Nonnull Runnable r` -> `final @Nonnull Runnable r`).

## Supported (auto-fixable)

| Pattern | Replacement |
| --- | --- |
| Single-line decl (`int x = 5;`) | `final int x = 5;` |
| Multi-var single-line (`int x, y;`) | `final int x, y;` (one insertion covers both variables) |
| Split decl, type on its own line (`List<String>` / `l = x;`) | `final` inserted on the type line |
| Split decl with blank line(s) between type and name | `final` inserted on the type line |
| Decl preceded by another statement on the same line (`X = a; int y = 0;`) | `final` inserted before the declaration's own type (`X = a; final int y = 0;`) |

## Not supported

| Pattern | Skip reason |
| --- | --- |
| Already `final` (single-line, or the split type line) | `cannot add 'final': the declaration already has it` |
| Split multi-var continuation (the line above the name's code ends in a top-level `,`, ignoring any trailing comment or literal contents) | `cannot add 'final': multi-variable declaration` (`final` applies to the whole declaration, so a non-first variable can't be made final on its own) |
| Split decl whose nearest non-blank line above the name does not start with a type/modifier/`@` | `cannot add 'final': no declaration type line precedes the variable name` (e.g. a comment-only line; `final` only legally precedes a type, modifier, or annotation) |
| Decl line was collapsed by a sibling fixer (e.g. `PreferMathMethodFixer` merged `int r;` + branched assign + trailing return into `return Math.max(...);`) | `cannot add 'final': no declaration type line precedes the variable name` (the original declaration line now starts with a control-flow keyword: `break`/`case`/`catch`/`continue`/`default`/`do`/`else`/`finally`/`for`/`if`/`return`/`switch`/`throw`/`try`/`while`/`yield`, which cannot legally take a `final` prefix) |

Part of [auto-fix coverage](../auto-fix-coverage.md).