# PreferDoWhileCheck auto-fix coverage

Collapses a duplicated pre-loop statement plus its `while` loop into a tier-2 `do-while`.

## Not supported

| Pattern | Reason |
| --- | --- |
| Comment on the pre-statement or body line | Comment preservation in the collapsed do-while is non-trivial |
| Pre-statement / body indent mismatch | Defensive; the happy path requires the same indent |
| Braced body has multiple statements or an unusual closing | Only single-statement braced bodies are collapsed |
| While line not in the expected single-line format | The fixer requires `while (cond)` (or `{`) on one line |
| Pre-statement and body not textually equal after stripping | Defensive; the check fired but the text differs (e.g. whitespace artifacts) |

Part of [auto-fix coverage](../auto-fix-coverage.md).