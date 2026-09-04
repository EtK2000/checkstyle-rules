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
| Buffer does not parse, or no `while` at the reported position | The tier cannot be read without a parse, so the form to emit is unknown (`cannot reshape the do-while body: its required form cannot be determined without a parse`) |
| Comment on the pre-statement or body line | Comment preservation in the collapsed do-while is non-trivial |
| Pre-statement not at the loop's indent | The pre-statement is matched against the loop body by text, so it must sit at the loop's indent |
| Blank line, a line indented deeper than the loop, or a statement not ending in `;` on its own line, directly before the loop | Only a complete single-line pre-statement can be compared with the loop body |
| Braced body has multiple statements or an unusual closing | Only single-statement braced bodies are collapsed |
| Loop body line that does not end in `;` (e.g. a body statement spanning lines) | Only a complete single-line body statement can be collapsed |
| While line not in the expected single-line format | The fixer requires `while (cond)` (or `{`) on one line |
| Pre-statement and body not textually equal after stripping | The check fires on statements that differ only in spacing, but the fixer compares text, so `i +=2;` before a body of `i += 2;` is refused |

Part of [auto-fix coverage](../auto-fix-coverage.md).