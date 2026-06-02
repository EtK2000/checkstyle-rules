# LambdaParameterTypeCheck auto-fix coverage

The fixer handles all three violation types in a single pass. For single non-annotated params, the
fixer goes straight to naked form (removing both type and parens).

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

String/char literals and comments on the lambda line are masked before the parameter list is
located and split, so a `->`, `(`, `)`, or `,` inside a literal or comment (e.g. an annotation
argument like `@A(")")` or a block comment between the parens and the type) does not misdirect the
fix; the literal content is preserved verbatim in the output.

## Not supported

| Pattern | Reason |
| --- | --- |
| Arrow `->` not found from the violation column | The fixer operates on the text from the column onward, so an arrow on a different line is skipped |
| Opening paren not found for the lambda parameters | A single naked parameter without parens, in unusual positions |

Part of [auto-fix coverage](../auto-fix-coverage.md).