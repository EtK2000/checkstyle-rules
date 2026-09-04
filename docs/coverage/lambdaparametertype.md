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

| Pattern | Reason |
| --- | --- |
| Arrow `->` not found from the violation column | An arrow on a different line from the violation column is skipped |
| Opening paren not found for the lambda parameters | The parameter list's `(` is on an earlier line than the arrow (`(String a,\n String b) ->`), so it is not on the line being fixed |

Part of [auto-fix coverage](../auto-fix-coverage.md).