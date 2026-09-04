# PreferDoWhileCheck auto-fix coverage

Collapses a duplicated pre-loop statement plus its `while` loop into a `do-while`.

Which of the two do-while forms is emitted is `ControlFlowBracesCheck`'s rule, read off the loop's
own body:

| Body | Form |
| --- | --- |
| Tier 2: `++i;`, `i += 2;`, `arr[i] = 0;`, `list.add(1);`, `node = node.next();` | `do <body>` on the `do` line, `while (cond);` next |
| Tier 3: binary right-hand side (`i = i + 1;`), `new` expression (`sb = new StringBuilder();`), chained call (`sb.append("a").append("b");`) | `do`, body on its own line indented one more, `while (cond);` |

A tier-2 body never takes braces; the tier only decides whether the body shares the `do` line.

## Not supported

| Pattern | Reason |
| --- | --- |
| Buffer does not parse, or no `while` at the reported position | The tier cannot be read without an AST, so the form to emit is unknown (`control.flow.skip.no.tier`) |
| Comment on the pre-statement or body line | Comment preservation in the collapsed do-while is non-trivial |
| Pre-statement / body indent mismatch | Defensive; the happy path requires the same indent |
| Braced body has multiple statements or an unusual closing | Only single-statement braced bodies are collapsed |
| While line not in the expected single-line format | The fixer requires `while (cond)` (or `{`) on one line |
| Pre-statement and body not textually equal after stripping | Defensive; the check fired but the text differs (e.g. whitespace artifacts) |

Part of [auto-fix coverage](../auto-fix-coverage.md).