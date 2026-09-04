# RedundantAnnotationSyntaxCheck auto-fix coverage

Removes empty parens (`@A()` / `@A ()` / `@A( )` -> `@A`) and a redundant `value =` key
(`@A(value = x)` -> `@A(x)`). Both boundaries come from node positions: the end of the annotation's
name, the closing parenthesis, the `value` identifier and the start of the value. Nothing is matched
against the line text, so the annotation's name may be spelled any way the language allows: with
whitespace or a comment after the `@`, with a package qualifier, with that qualifier split across
lines, or with characters outside ASCII.

Removing the parens inserts a separating space when that would otherwise fuse the name, or a
preserved comment, into what follows (`@A()String` -> `@A String`, `@A()@B` -> `@A @B`,
`@B(/* c */)String` -> `@B /* c */ String`), but not before whitespace, punctuation or end of line.
A comment inside the parens, or between the name and them, is re-emitted after the name
(`@A(/* c */)` -> `@A /* c */`).

An annotation written across lines is folded onto one when that is safe. For empty parens, a
comment-only line between `@A(` and `)` folds after the name in source order; a `//` comment folds
too, but only as the last content on the merged line, otherwise the annotation is left alone so a
later comment or trailing code is not swallowed. For a `value =` written across lines the whole
annotation collapses onto one line, with comments on either side of the value kept on the side they
were written; when that collapse is refused, only `value = ` is stripped and the annotation keeps
the lines it had. A fold whose result would be longer than the line-length budget is refused unless
the source was already past it, so the fixer never hands back a line it pushed over the limit.

Decoy `@X()` / `(value =` / `value =` sequences inside a string, comment or text block are never
matched, because nothing is matched: they are not annotations in the parse. A `value = ...`
statement below a closed annotation is likewise untouched.

## Not supported

| Pattern | Reason |
| --- | --- |
| Multiline empty parens where a `//` line comment is not the last content on the merged line | A later comment, or code after the `)`, would be swallowed |
| Multiline empty parens with a block comment spanning line boundaries between the parens | It cannot be written on one line |
| Multiline `value =` holding a `//` comment or a line-crossing block comment outside the region between the `value` key and its value | The collapse is refused; only `value = ` is stripped, leaving the annotation multiline |
| Multiline `value =` with a `//` comment or a line-crossing block comment between the `value` key and its value | Refused outright, nothing is stripped: deleting the key would join those two lines, and the comment forbids it |
| Multiline `value =` whose value is a text block | A text block's opening delimiter has to be the last thing on its line, so collapsing would delete the line break it depends on. Only `value = ` is stripped |
| A fold whose result would exceed the line-length budget | The redundancy would come off at the cost of a line the project's own `LineLength` rule rejects. For a multi-line `value =` the fold is simply refused and the key still comes off its own line, so the fixer never joins lines into one longer than any it replaced. Taking the parentheses off has no such fall-back, so there a span already past the budget is exempt: refusing it would keep the redundant syntax for good |
| A position whose text has moved under an earlier fix in the same pass | Nothing the check recognises sits at the reported position any more, so the fix is left for the next pass |
| A buffer that does not parse | The classifier needs the parse. The fix is retried once the buffer parses |

Part of [auto-fix coverage](../auto-fix-coverage.md).