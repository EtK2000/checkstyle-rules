# PreferDoWhileCheck auto-fix coverage

Collapses a duplicated pre-loop statement plus its `while` loop into a `do-while`.

Which of the two do-while forms is emitted is `ControlFlowBracesCheck`'s rule, read off the loop's
own body:

| Body | Form |
| --- | --- |
| Tier 2: `++i;`, `i += 2;`, `arr[i] = 0;`, `list.add(1);`, `node = node.next();` | `do <body>` on the `do` line, `while (cond);` next |
| Tier 3: binary right-hand side (`i = i + 1;`), `new` expression (`sb = new StringBuilder();`), chained call (`sb.append("a").append("b");`) | `do`, body on its own line indented one more, `while (cond);` |

A tier-2 body never takes braces; the tier only decides whether the body shares the `do` line.

The rewrite replaces everything from the pre-statement's first token to the loop's last, keeping
only the condition and the body. The emitted indentation is the statement's canonical one rather
than a copy of the replaced text's, so a pre-statement or loop written at the wrong indent is
corrected, not reproduced. The loop body's spelling is the one emitted, since the check fires on structural
equality and two statements differing only in spacing are the same statement.

## Supported (auto-fixable)

| Pattern | Result |
| --- | --- |
| Pre-statement and body differing only in whitespace | Fixed, emitting the body's spelling |
| Pre-statement or loop at an unexpected indent | Fixed at the indent the statement belongs at |
| Blank line between the pre-statement and the loop | Fixed; the blank line is absorbed |
| Multi-line `while` condition | Fixed; the condition is folded onto the emitted `while (cond);` |
| Multi-line pre-statement or body statement | Fixed; the body is folded onto one line |
| Comment anywhere inside the collapsed region | Fixed; the displaced comments merge into one comment trailing the emitted `while (cond);` |
| Comment between the body's last token and the body's `;` | Fixed; it stays where it is, since the body is copied through its terminator rather than displaced |
| Comment after the body's `;`, or content after a braced body's `}` | Fixed; it lands after the emitted `while (cond);`, having never been inside the region |
| Braced body whose `}` is at an unexpected indent or carries trailing content | Fixed |
| Something ahead of the pre-statement on its line (a `case` label, an earlier statement) | Fixed; that line is kept and the do-while starts below it |
| That kept line is itself longer than the project's line budget | Fixed; only the emitted do-while is measured, since the kept line is source the fold merely shortened |

## Not supported

| Pattern | Reason |
| --- | --- |
| Buffer does not parse, or no reportable `while` at the position | Nothing to classify, so no fix is attempted |
| Text block opened inside the collapsed region, or a comment inside a multi-line condition or body | Folding would orphan the rest of the literal, or comment out the folded code (`cannot convert to do-while: a text block or comment inside the loop cannot survive the collapse`) |
| An emitted line longer than the project's line budget | Nothing downstream wraps it, and the fold is what made it long (`cannot convert to do-while: the collapsed loop would exceed the line length limit`) |

Part of [auto-fix coverage](../auto-fix-coverage.md).