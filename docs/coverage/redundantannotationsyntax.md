# RedundantAnnotationSyntaxCheck auto-fix coverage

Removes empty parens (`@A()` / `@A ()` / `@A( )` -> `@A`), inserting a separating space when removal would otherwise fuse the name (or a preserved comment) into a following identifier or annotation (`@A()String` -> `@A String`, `@A()@B` -> `@A @B`, `@B(/* c */)String` -> `@B /* c */ String`), but not before whitespace or end of line. A comment inside or between the name and the parens is preserved, re-emitted after the name (`@A(/* c */)` -> `@A /* c */`). Also removes a redundant `value =` key (`@A(value = x)` -> `@A(x)`). Single-line and multiline.

For multiline empty parens, comment-only lines between `@A(` and `)` are folded after the name in source order; a `//` line comment folds too, but only when it is the last content on the merged line (otherwise the annotation is left multiline, so a later comment or trailing code is not swallowed). For a multiline `value =`, the whole annotation collapses onto one line when the span has no `//` comment, no block comment crossing a line boundary, and the collapsed line fits the max width; otherwise only `value = ` is stripped on its line, leaving the annotation multiline.

Decoy `@X()` / `(value =` / `value =` sequences inside a string, comment, or text block are never matched, and a `value =` outside the annotation's parens (a closed annotation) is never rewritten.

## Not supported

| Pattern | Reason |
| --- | --- |
| Multiline empty parens where a `//` line comment is not the last content on the merged line | A later comment, or code after the `)`, would be swallowed |
| Multiline empty parens with a block comment spanning line boundaries between the parens | It cannot be collapsed onto one line |
| Multiline empty parens with code between the parens | The parens are not empty |
| Open `@A(` with no `)` before EOF | The scan stops rather than corrupt or delete content |
| Multiline `value =` with a non-blank, non-`value =` continuation line before the value | The scan stops rather than corrupt or delete content |

Part of [auto-fix coverage](../auto-fix-coverage.md).