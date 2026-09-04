# LambdaParameterTypeCheck auto-fix coverage

The fixer handles all three violation types in a single pass.

| Violation type | Pattern | Replacement |
| --- | --- | --- |
| Unnecessary parens | `(x) ->` | `x ->` |
| Use implicit (single) | `(String x) ->` / `(var x) ->` | `x ->` |
| Use implicit (multi) | `(String x, int y) ->` / `(var x, var y) ->` | `(x, y) ->` |
| Use var (single) | `(@A String x) ->` | `(@A var x) ->` |
| Use var (multi mixed) | `(@A String x, String y) ->` | `(@A var x, var y) ->` |
| Use var (multi both) | `(@A String x, @B String y) ->` | `(@A var x, @B var y) ->` |
| Use implicit (C-style array) | `(int a[], int y) ->` / `(int a[][], int y) ->` / `(int a [], int y) ->` | `(a, y) ->` |
| Use var (C-style array) | `(@A int a[], String y) ->` | `(@A var a, var y) ->` |
| Use implicit (list across lines) | `(String x,`<br>`String y) ->` | `(x, y) ->` on the list's first line |
| Use var (list across lines) | `(@A String x,`<br>`@B String y) ->` | `(@A var x, @B var y) ->` on the first line |
| Unnecessary parens (list across lines) | `(`<br>`x) ->` | `x ->` on the first line |

A parameter list written across lines is folded onto the line its `(` sits on, and the text before
the `(` and after the `)` is carried through verbatim, including comments on either side of the list
(`/* note */ (String x,` ... `String y) ->` keeps `/* note */`). The break may fall anywhere: after
the `(`, after a comma, before the `)`, or with the `->` on a line of its own after the `)`. When the
`->` is on a later line than the list, only the list is rewritten and the arrow's line is untouched.

Removing the explicit type from a lambda parameter drops any trailing C-style array brackets too
(`int a[]` -> `a`, not `a[]`), regardless of whitespace before the bracket. Java-style brackets on
the type (`int[] a`) are unaffected since they precede the name.

A `->`, `(`, `)`, or `,` inside a string/char literal or comment on the lambda line (e.g. an
annotation argument like `@A(")")` or a block comment between the parens and the type) does not
misdirect the fix; the literal content is preserved verbatim in the output. The list is split only on commas
outside type arguments, annotation arguments and array brackets, so `(Map<String, Integer> m,
Integer x) ->` becomes `(m, x) ->` and `(@A(x = 1, y = 2) Map<String, Integer> m, Integer x) ->`
becomes `(@A(x = 1, y = 2) var m, var x) ->`.

A block comment between the type and the name, or trailing the name, is not mistaken for the name
itself.

**A comment inside a parameter is dropped when the type goes with it** (`(String x /*c*/) ->`
becomes `x ->`, and `(/*c*/ String x) ->` becomes `x ->`). A comment *before the annotations* of an
annotated parameter is kept (`(/*c*/ @A String x) ->` becomes `(/*c*/ @A var x) ->`).

## Not supported

Each pattern below is left alone by the fixer *and* goes unreported by the check, so it costs no
violation rather than one that cannot be cleared.

| Pattern | Reason |
| --- | --- |
| A comment inside a list written across lines (`(String x, // note`) | Folding the list onto one line would delete the comment |
| A string literal inside a list written across lines (`(@A("x") String x,`) | Same: folding would move the literal's line away. A literal whose content is only whitespace (`@A(" ")`) is folded normally |
| A fold that would exceed 120 columns | Unless the folded line is no wider than the line it replaces, in which case it is folded even past the limit |

A list already on one line is never refused for any of these: nothing moves, so a comment there goes
with the type it annotates (see above) and the length only ever shrinks.

Part of [auto-fix coverage](../auto-fix-coverage.md).